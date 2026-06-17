#!/bin/bash

# ==============================================
# NBANK - Полный скрипт развертывания
# ==============================================

echo "🚀 Starting NBank deployment..."

# ==============================================
# ШАГ 1: Запуск Kubernetes кластера
# ==============================================
# Запустили локальный Kubernetes-кластер с помощью minikube, используя Docker как драйвер
# (кластер будет запущен внутри докер контейнера - можно также запускать и на виртуальной машине)
echo "📦 Starting Minikube..."
minikube start --driver=docker

# ==============================================
# ШАГ 2: Развертывание NBank приложения
# ==============================================
echo "📦 Deploying NBank application..."

# Создаем ConfigMap для Selenoid
# Создали ConfigMap с именем selenoid-config, файл будет доступен под ключом browsers.json
kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

# Устанавливаем Helm чарт с именем релиза nbank (шаблоны берем из ./nbank-chart)
# - Это создаст все ресурсы, описанные в шаблонах Helm (Deployment, Service и тд)
helm install nbank ./nbank-chart

# Проверка статуса:
echo "📊 Checking application status..."
kubectl get svc
kubectl get pods

# ==============================================
# ШАГ 3: Проброс портов для приложения (фоновый режим)
# ==============================================
echo "🔌 Starting port-forwarding for application..."
# Убиваем старые процессы port-forward
pkill -f "kubectl port-forward" 2>/dev/null || true

# Запускаем в фоновом режиме с подавлением вывода
kubectl port-forward svc/frontend 3000:80 > /dev/null 2>&1 &
kubectl port-forward svc/backend 4111:4111 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid 4444:4444 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid-ui 8080:8080 > /dev/null 2>&1 &

sleep 3
echo "✅ Port-forwarding started:"
echo "  - Frontend: http://localhost:3000"
echo "  - Backend: http://localhost:4111"
echo "  - Selenoid: http://localhost:4444"
echo "  - Selenoid UI: http://localhost:8080"


# ==============================================
# ШАГ 4: Мониторинг (Prometheus + Grafana)
# ==============================================
echo "📊 Deploying monitoring stack..."

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add elastic https://helm.elastic.co || true
helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f monitoring-values.yaml

# Проброс портов для мониторинга
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 3001:9090 > /dev/null 2>&1 &
kubectl port-forward svc/monitoring-grafana -n monitoring 3002:80 > /dev/null 2>&1 &

# Создаем секреты для авторизации на бекенде
kubectl create secret generic backend-basic-auth --from-literal=username=admin --from-literal=password=admin -n monitoring

# Применяем конфигурацию SpringMonitoring
kubectl apply -f spring-monitoring.yaml

echo "✅ Monitoring stack ready:"
echo "  - Prometheus: http://localhost:3001"
echo "  - Grafana: http://localhost:3002 (admin:prom-operator)"

# ==============================================
# ШАГ 5: Elasticsearch + Kibana + Filebeat
# ==============================================
echo "📊 Deploying logging stack (Elasticsearch + Kibana + Filebeat)..."

helm repo add elastic https://helm.elastic.co || true
helm repo update

# Создаем namespace для логирования
kubectl create namespace logging

# Устанавливаем Elasticsearch (безопасность отключена для простоты)
echo "📦 Installing Elasticsearch..."
helm upgrade --install elasticsearch elastic/elasticsearch -n logging \
  --set replicas=1 \
  --set resources.requests.memory=1Gi \
  --set resources.requests.cpu=500m \
  --set esJavaOpts="-Xmx512m -Xms512m" \
  --set persistence.size=10Gi \
  --set esConfig.elasticsearch.yml="xpack.security.enabled: false\ndiscovery.type: single-node" \
  --wait

# Устанавливаем Kibana (без хуков, чтобы избежать зависаний)
echo "📦 Installing Kibana..."
helm upgrade --install kibana elastic/kibana -n logging \
  --set elasticsearchHosts="http://elasticsearch-master:9200" \
  --set resources.requests.memory=512Mi \
  --set resources.requests.cpu=200m \
  --set healthCheck.enabled=false \
  --set hooks.enabled=false \
  --wait

# Устанавливаем Filebeat (через простой манифест для надежности)
echo "📦 Installing Filebeat..."
kubectl apply -f filebeat-final.yaml

# Получить пароль
#kubectl get secrets -n logging elasticsearch-master-credentials -ojsonpath='{.data.password}' | base64 -d
#echo ""

# Получить имя пользователя (обычно "elastic")
#kubectl get secrets -n logging elasticsearch-master-credentials -ojsonpath='{.data.username}' | base64 -d
#echo ""

# Ждем готовности Filebeat
echo "⏳ Waiting for Filebeat to be ready..."
kubectl wait --for=condition=ready pod -l app=filebeat -n logging --timeout=60s 2>/dev/null || true

# Проброс порта Kibana
kubectl port-forward svc/kibana 5601:5601 -n logging > /dev/null 2>&1 &

sleep 3
echo "✅ Logging stack ready:"
echo "  - Elasticsearch: http://localhost:9200"
echo "  - Kibana: http://localhost:5601"

# ==============================================
# ШАГ 6: Проверка работоспособности
# ==============================================
echo "🔍 Verifying deployment..."

# Проверяем поды
echo ""
echo "📊 All pods:"
kubectl get pods --all-namespaces | grep -E "backend|frontend|selenoid|elasticsearch|kibana|filebeat|monitoring"

# Проверяем логи бэкенда
echo ""
echo "📋 Backend logs (last 3 lines):"
kubectl logs deployment/backend --tail=3 2>/dev/null || echo "Backend not ready yet"

# Проверяем логи Filebeat
echo ""
echo "📋 Filebeat logs (last 3 lines):"
kubectl logs -n logging daemonset/filebeat --tail=3 2>/dev/null || echo "Filebeat not ready yet"

# Проверяем индексы в Elasticsearch
echo ""
echo "📊 Elasticsearch indices:"
curl -s "http://localhost:9200/_cat/indices?v" | grep -E "filebeat-backend|filebeat-" || echo "No indices yet"

# ==============================================
# ШАГ 7: Итоговая информация
# ==============================================
echo ""
echo "=============================================="
echo "✅ NBANK DEPLOYMENT COMPLETE!"
echo "=============================================="
echo ""
echo "🌐 Application URLs:"
echo "  - Frontend:    http://localhost:3000"
echo "  - Backend API: http://localhost:4111"
echo "  - Selenoid:    http://localhost:4444"
echo "  - Selenoid UI: http://localhost:8080"
echo ""
echo "📊 Monitoring:"
echo "  - Prometheus:  http://localhost:3001"
echo "  - Grafana:     http://localhost:3002 (admin:prom-operator)"
echo ""
echo "📊 Logging:"
echo "  - Kibana:      http://localhost:5601"
echo "  - Elasticsearch: http://localhost:9200"
echo ""
echo "🔑 Credentials:"
echo "  - Backend:     admin/admin"
echo "  - Kibana:      No auth (security disabled)"
echo ""
echo "📝 Commands:"
echo "  - Check pods:        kubectl get pods"
echo "  - Check logs:        kubectl logs deployment/backend"
echo "  - Check Filebeat:    kubectl logs -n logging daemonset/filebeat"
echo "  - Check Elasticsearch: curl http://localhost:9200/_cat/indices?v"
echo ""
echo "⚠️  To stop port-forwarding: pkill -f 'kubectl port-forward'"
echo "=============================================="

