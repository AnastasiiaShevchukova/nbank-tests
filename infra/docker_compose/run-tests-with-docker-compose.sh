#!/bin/bash

IMAGE_NAME=nbank-tests
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

API_OUTPUT_DIR="$TEST_OUTPUT_DIR/api"
UI_OUTPUT_DIR="$TEST_OUTPUT_DIR/ui"

API_RESULT=0
UI_RESULT=0

cleanup() {
echo ""
echo ">>> Остановка тестового окружения"
docker compose down
}

# Выполнить cleanup при любом завершении скрипта
trap cleanup EXIT

echo ">>> Поднятие тестового окружения"
docker compose up -d

docker network ls

echo ">>> Ожидание запуска сервисов"
sleep 15

#Создаем папки для API тестов
mkdir -p "$API_OUTPUT_DIR/logs"
mkdir -p "$API_OUTPUT_DIR/results"
mkdir -p "$API_OUTPUT_DIR/report"

#Создаем папки для UI тестов
mkdir -p "$UI_OUTPUT_DIR/logs"
mkdir -p "$UI_OUTPUT_DIR/results"
mkdir -p "$UI_OUTPUT_DIR/report"

# Диагностика
echo "APIBASEURL=http://backend:4111"
echo "UIBASEURL=http://nginx"
echo "UIREMOTE=http://selenoid:4444/wd/hub"

echo ">>> ЗАПУСК API ТЕСТОВ"
docker run --rm \
--network nbank-network \
-v "$API_OUTPUT_DIR/logs":/app/logs \
-v "$API_OUTPUT_DIR/results":/app/target/surefire-reports \
-v "$API_OUTPUT_DIR/report":/app/target/site \
-e TEST_PROFILE=api \
-e APIBASEURL=http://backend:4111 \
-e UIBASEURL=http://nginx \
-e UIREMOTE=http://selenoid:4444/wd/hub \
$IMAGE_NAME || API_RESULT=$?

echo ">>> ЗАПУСК UI ТЕСТОВ"
docker run --rm \
--network nbank-network \
-v "$UI_OUTPUT_DIR/logs":/app/logs \
-v "$UI_OUTPUT_DIR/results":/app/target/surefire-reports \
-v "$UI_OUTPUT_DIR/report":/app/target/site \
-e TEST_PROFILE=ui \
-e APIBASEURL=http://backend:4111 \
-e UIBASEURL=http://nginx \
-e UIREMOTE=http://selenoid:4444/wd/hub \
$IMAGE_NAME || UI_RESULT=$?

echo ""
echo "API:"
echo " Лог: $API_OUTPUT_DIR/logs/run.log"
echo " Результаты: $API_OUTPUT_DIR/results"
echo " Отчет: $API_OUTPUT_DIR/report"

echo ""
echo "UI:"
echo " Лог: $UI_OUTPUT_DIR/logs/run.log"
echo " Результаты: $UI_OUTPUT_DIR/results"
echo " Отчет: $UI_OUTPUT_DIR/report"

echo ""
echo "Коды завершения:"
echo "API = $API_RESULT"
echo "UI = $UI_RESULT"

if [ $API_RESULT -ne 0 ] || [ $UI_RESULT -ne 0 ]; then
echo ">>> Есть упавшие тесты"
exit 1
fi

echo ">>> Все тесты успешно завершены"
exit 0