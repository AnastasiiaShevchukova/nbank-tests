#!/bin/bash

echo "🛑 Stopping NBank services..."

# Убиваем все port-forward процессы
pkill -f "kubectl port-forward"

# Останавливаем Minikube
minikube stop

echo "✅ All services stopped."