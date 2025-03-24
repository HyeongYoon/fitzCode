#!/bin/bash

# 변수 설정
DOCKER_USERNAME=""
APP_IMAGE_NAME="fizCodeApp"
APP_CONTAINER_NAME="fitzCodeWeb"
VERSION="latest"
PORT_HOST=80
PORT_CONTAINER=8080

# .env 파일 로드
if [ -f .env ]; then
    source .env
else
    echo "Error: .env file not found!"
    exit 1
fi

echo "## Automation Docker Build and Run ##"
echo "=> Remove previous container..."
docker rm -f ${APP_CONTAINER_NAME} || true
echo "=> Remove previous image..."
docker rmi -f ${APP_IMAGE_NAME}:${VERSION} || true
echo "=> Build new image..."
docker build --tag ${APP_IMAGE_NAME}:${VERSION} -f Dockerfile .
echo "=> Run container with environment variables..."
docker run -d -p ${PORT_HOST}:${PORT_CONTAINER} \
    --name ${APP_CONTAINER_NAME} \
    -e ENCRYPTOR_KEY="${ENCRYPTOR_KEY}" \
    ${APP_IMAGE_NAME}:${VERSION}
echo "=> Done! Check http://${EC2_PUBLIC_IP}:${PORT_HOST} or http://${DOMAIN}"