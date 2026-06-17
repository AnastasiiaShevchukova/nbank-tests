# NBank - Banking Application with Full Observability Stack

## 📋 Описание проекта

NBank - это учебное банковское приложение, развернутое в Kubernetes с полным стеком наблюдения (Observability), включая мониторинг (Prometheus + Grafana) и логирование (Elasticsearch + Kibana + Filebeat).

### Архитектура
┌─────────────────────────────────────────────────────────────────┐
│ Kubernetes Cluster │
│ │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │
│ │ Frontend │ │ Backend │ │ Selenoid │ │
│ │ (Nginx) │ │ (Spring Boot)│ │ (UI Tests) │ │
│ └──────┬───────┘ └──────┬───────┘ └──────────┬───────────┘ │
│ │ │ │ │
│ └─────────────────┼──────────────────────┘ │
│ │ │
│ ┌────────────────────────▼────────────────────────────────────┐ │
│ │ Monitoring Stack │ │
│ │ ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐ │ │
│ │ │ Prometheus │ │ Grafana │ │ Spring Monitoring │ │ │
│ │ └──────────────┘ └──────────────┘ └──────────────────┘ │ │
│ └─────────────────────────────────────────────────────────────┘ │
│ │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ Logging Stack │ │
│ │ ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐ │ │
│ │ │ Elasticsearch│ │ Kibana │ │ Filebeat │ │ │
│ │ │ (Storage) │ │ (UI/Visual) │ │ (Collector) │ │ │
│ │ └──────────────┘ └──────────────┘ └──────────────────┘ │ │
│ └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

## 🚀 Быстрый старт

### Предварительные требования

- [Docker](https://www.docker.com/products/docker-desktop/)
- [Minikube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Helm](https://helm.sh/docs/intro/install/)
- [jq](https://stedolan.github.io/jq/) (для работы с JSON)

### Установка

```bash
# 1. Клонировать репозиторий
git clone <repository-url>
cd nbank-tests

# 2. Запустить полное развертывание
cd infra/kube
./restart_kube.sh

# 3. Дождаться завершения (около 5-10 минут)

```

## 🎯 Доступ к сервисам

### Приложение
| Сервис | URL | Описание |
|--------|-----|----------|
| Frontend | `http://localhost:3000` | Web интерфейс |
| Backend API | `http://localhost:4111` | REST API |
| Selenoid | `http://localhost:4444` | UI тестирование |
| Selenoid UI | `http://localhost:8080` | Интерфейс Selenoid |
### Мониторинг
| Сервис | URL | Описание | Credentials |
|--------|-----|----------|-------------|
| Prometheus | `http://localhost:3001` | Сбор метрик | - |
| Grafana | `http://localhost:3002` | Визуализация метрик | `admin` / `prom-operator` |
### Логирование
| Сервис | URL | Описание |
|--------|-----|----------|
| Kibana | `http://localhost:5601` | Анализ логов |
| Elasticsearch | `http://localhost:9200` | API для логов |

#### Настройка Kibana:
1. Открыть http://localhost:5601
2. Stack Management → Index Patterns
3. Создать: filebeat-backend-*
4. Поле времени: timestamp
5. Discover → поиск: message: "Deposit"

## 📊 Использование
### Генерация тестовых данных
```bash
cd test-scripts
./daily_activity.bash 
```

Скрипт создает:
- Случайных пользователей (1-10, иногда 40-100)
- Аккаунты для пользователей
- Переводы (минимум 50)
- Пополнения счетов

Проверка логов
``` bash
# Посмотреть логи бэкенда
kubectl logs deployment/backend --tail=20

# Посмотреть логи Filebeat
kubectl logs -n logging daemonset/filebeat --tail=20

# Проверить индексы в Elasticsearch
curl http://localhost:9200/_cat/indices?v

# Поиск логов бэкенда
curl -X POST "http://localhost:9200/filebeat-backend-*/_search" \
  -H "Content-Type: application/json" \
  -d '{"query": {"match": {"message": "Deposit"}},"size": 5}' | jq '.'
  ```

## 📁 Структура проекта
```plaintext
nbank-tests/
├── infra/
│   └── kube/
│       ├── restart_kube.sh          # Основной скрипт развертывания
│       ├── stop_kube.sh             # Скрипт остановки
│       ├── filebeat-final.yaml      # Конфигурация Filebeat
│       ├── monitoring-values.yaml   # Конфигурация Prometheus/Grafana
│       ├── spring-monitoring.yaml   # Мониторинг Spring приложения
│       └── README.md                # Этот файл
├── test-scripts/
│   └── daily_activity.bash          # Скрипт генерации тестовых данных
├── nbank-chart/                     # Helm чарт NBank
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
│       ├── backend.yaml
│       ├── frontend.yaml
│       ├── selenoid.yaml
│       └── selenoid-ui.yaml
└── nbank-tests.iml
```


## 🔍 Основные команды

### Управление кластером
``` bash
# Запустить кластер
./restart_kube.sh

# Остановить кластер
./stop_kube.sh

# Проверить статус подов
kubectl get pods --all-namespaces

# Перезапустить бэкенд
kubectl rollout restart deployment/backend
```

### Работа с логами
```bash
# Смотреть логи в реальном времени
kubectl logs deployment/backend -f

# Смотреть логи Filebeat
kubectl logs -n logging daemonset/filebeat -f

# Смотреть логи Kibana
kubectl logs -n logging deployment/kibana -f
```
### Работа с Elasticsearch
```bash
# Просмотр индексов
curl http://localhost:9200/_cat/indices?v

# Удалить все логи бэкенда
curl -X DELETE "http://localhost:9200/filebeat-backend-*"

# Поиск ошибок
curl -X POST "http://localhost:9200/filebeat-backend-*/_search" \
  -H "Content-Type: application/json" \
  -d '{"query": {"term": {"level": "ERROR"}}}' | jq '.'
  ```
### Работа с портами
```bash
# Проверить занятые порты
lsof -i :9200  # Elasticsearch
lsof -i :5601  # Kibana
lsof -i :3000  # Frontend
lsof -i :4111  # Backend

# Остановить все port-forward
pkill -f "kubectl port-forward"
```
## 🐛 Устранение неполадок
### Filebeat не собирает логи
```bash
# Проверить, что Filebeat видит файлы логов
kubectl exec -n logging daemonset/filebeat -- ls -la /var/log/containers/ | grep backend

# Проверить логи Filebeat
kubectl logs -n logging daemonset/filebeat --tail=50

# Перезапустить Filebeat
kubectl delete pod -n logging -l app=filebeat
```
### Kibana не показывает логи
1. Проверьте создан ли Index Pattern: filebeat-backend-*
2. Проверьте поле времени: timestamp
3. Выберите правильный временной диапазон: "Last 7 days"
4. Обновите страницу (Refresh)

### Elasticsearch не запускается
```bash
# Проверить логи
kubectl logs -n logging elasticsearch-0

# Если не хватает памяти
minikube stop
minikube start --memory=4096 --cpus=4
```

### Port-forward не работает
```bash
# Проверить процессы
ps aux | grep "kubectl port-forward"

# Перезапустить все port-forward
pkill -f "kubectl port-forward"
./restart_kube.sh
```
## 🔐 Безопасность
- Elasticsearch: безопасность отключена (для разработки)
- Kibana: без авторизации
- Backend: admin / admin


## 📈 Производительность
### Требования к ресурсам
| Компонент | CPU | Memory | Storage |
|-----------|-----|--------|---------|
| Minikube | 4 cores | 4 GB | 20 GB |
| Elasticsearch | 500m | 1 GB | 10 GB |
| Kibana | 200m | 512 MB | - |
| Filebeat | 100m | 100 MB | - |
| Backend | 200m | 512 MB | - |
| Frontend | 100m | 128 MB | - |
| Prometheus | 200m | 512 MB | 5 GB |
| Grafana | 100m | 256 MB | - |
| **Итого** | **~5.4 cores** | **~6.5 GB** | **~35 GB** |

## 📊 Мониторинг в Grafana
1. Открыть http://localhost:3002
2. Логин: admin / prom-operator
3. Добавить Data Source: Prometheus (http://prometheus:9090)
4. Импортировать Dashboard для Spring Boot (ID: 12900)

## 🔧 Настройка Kibana
### Индексные паттерны
1. filebeat-backend-* - логи бэкенда
2. filebeat-* - системные логи

### Популярные запросы в Kibana
| Запрос | Описание |
|--------|----------|
| `message: "Deposit"` | Пополнения счетов |
| `message: "transfer"` | Переводы |
| `level: "ERROR"` | Ошибки |
| `message: "Created user"` | Создание пользователей |
| `message: "Account created"` | Создание аккаунтов |
| `message: "transfer" AND level: "ERROR"` | Ошибки при переводах |
| `message: "user*"` | Логи связанные с пользователями |
| `message: "login"` | Логины пользователей |
| `kubernetes.labels.app: "backend"` | Только логи бэкенда |
| `@timestamp: [now-1h TO now]` | Логи за последний час |

