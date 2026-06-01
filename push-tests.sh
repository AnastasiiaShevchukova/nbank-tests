#!/bin/bash

set -e

# Настройка
IMAGE_NAME="nbank-tests"
DOCKERHUB_USERNAME="caporegime"
TAG="${1:-latest}"

# Проверка токена
if [ -z "$DOCKERHUB_TOKEN" ]; then
echo "Ошибка: переменная DOCKERHUB_TOKEN не установлена"
exit 1
fi

FULL_IMAGE_NAME="${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${TAG}"

echo ">>> Авторизация в Docker Hub"
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

echo ">>> Тегирование образа"
docker tag "${IMAGE_NAME}" "$FULL_IMAGE_NAME"

echo ">>> Отправка образа в Docker Hub"
docker push "$FULL_IMAGE_NAME"

echo ">>> Готово"
echo "Образ опубликован:"
echo "$FULL_IMAGE_NAME"
echo
echo "Для скачивания:"
echo "docker pull $FULL_IMAGE_NAME"