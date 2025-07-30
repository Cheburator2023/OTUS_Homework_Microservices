## Инструкции по запуску и тестированию:

### 1. Установка БД из Helm:
#### Добавьте репозиторий Bitnami
helm repo add bitnami https://raw.githubusercontent.com/bitnami/charts/archive-full-index/bitnami

#### Создание namespace
kubectl create namespace user-app

#### Запуск PostgreSQL
helm install postgres bitnami/postgresql -n user-app \
--set auth.database=user_db \
--set auth.username=postgres \
--set auth.password=postgres \
--set primary.persistence.enabled=false \
--set volumePermissions.enabled=true

### 2. Запуск Ingress, Prometheus и Grafana:
#### Запуск Ingress
#### Создание namespace
kubectl create namespace ingress-nginx

#### Запуск Ingress
helm install ingress-nginx ingress-nginx/ingress-nginx \
--namespace ingress-nginx \
--set controller.metrics.enabled=true \
--set controller.metrics.serviceMonitor.enabled=true \
--set controller.metrics.serviceMonitor.additionalLabels.release="prometheus" \
--set controller.podAnnotations."prometheus\.io/scrape"="true" \
--set controller.podAnnotations."prometheus\.io/port"="10254"

#### Проброс внешнего порта из minikube
minikube tunnel

#### Запуск Prometheus
helm install prometheus ./charts/prometheus -n user-app

#### Запуск Grafana
helm install grafana ./charts/grafana -n user-app

### 3. Запуск приложения:

#### Сборка образа:
docker build -t yourusername/user-app:latest .

#### Загрузка образа на DockerHub
docker push yourusername/user-app:latest

#### Запуск приложения:
helm install user-app ./user/charts/user-app

### 4. Проверка работы:
#### Проверить поды:
kubectl get pod -n user-app -o wide

#### Проверить логи:
kubectl logs -f <pod-name>

#### Проверить сервис:
kubectl get svc -n user-app

#### Проверить ingress:
kubectl get all -n ingress-nginx

### 5. Запуск Postman коллекции:
newman run user-app.postman_collection.json

### 6. Запуск тестов

#### Установка K6
choco install k6 -y --force

#### Запуск нагрузочных тестов
k6 run tests/load/k6/scenarios/api/test.js
