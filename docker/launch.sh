#!/bin/bash
REGISTRY_URL=192.168.199.135:2375
WORK_DIR=/root/work_build
PROJECT_NAME=eshop-sync-service
if [ ! -e ${WORK_DIR}/${PROJECT_NAME} ] && [ ! -d ${WORK_DIR}/${PROJECT_NAME} ]; then
mkdir -p ${WORK_DIR}/${PROJECT_NAME}
echo "Create Dir: ${WORK_DIR}/${PROJECT_NAME}"
fi
if [ -e ${WORK_DIR}/${PROJECT_NAME}/Dockerfile ]; then
rm -rf ${WORK_DIR}/${PROJECT_NAME}/Dockerfile
echo "Remove File: ${WORK_DIR}/${PROJECT_NAME}/Dockerfile"
fi
cp /root/.jenkins/workspace/${PROJECT_NAME}/docker/Dockerfile ${WORK_DIR}/${PROJECT_NAME}/
cp /root/.jenkins/workspace/${PROJECT_NAME}/build/libs/*.jar ${WORK_DIR}/${PROJECT_NAME}/
cd ${WORK_DIR}/${PROJECT_NAME}/
docker build -t ${REGISTRY_URL}/eshop-detail/${PROJECT_NAME} .
docker push ${REGISTRY_URL}/eshop-detail/${PROJECT_NAME}
if docker ps -a | grep ${PROJECT_NAME}; then
docker rm -f ${PROJECT_NAME}
echo "Remove Docker Container: ${PROJECT_NAME}"
fi
docker run -d -p 8771:8771 --name ${PROJECT_NAME} ${REGISTRY_URL}/eshop-detail/${PROJECT_NAME}