## Инструкции по запуску и тестированию:

### 1. Установка БД из Helm:
# Репозиторий Bitnami для PostgreSQL
helm repo add bitnami https://charts.bitnami.com/bitnami

# Репозиторий Ingress Nginx
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx

# Обновление репозиториев
helm repo update

#### Создание namespace
kubectl create namespace microservices

#### Запуск PostgreSQL
helm install postgres bitnami/postgresql -n microservices \
--set auth.database=user_db \
--set auth.username=postgres \
--set auth.password=postgres \
--set primary.persistence.enabled=false \
--set volumePermissions.enabled=true

# Получение имени пода PostgreSQL
kubectl get pods -n microservices -l app.kubernetes.io/name=postgresql -o name

# Создание баз данных
kubectl exec -n microservices -it <pod-name> -- bash
psql -U postgres
CREATE DATABASE billing_db;
CREATE DATABASE notification_db;
CREATE DATABASE order_db;
\q
exit
### 2. Запуск Ingress, Prometheus и Grafana:

#### 2.1 Установка CRD для ServiceMonitor
kubectl apply -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_servicemonitors.yaml

#### 2.2 Подготовка к установке Ingress

#### Создаём namespace для ingress-nginx (если ещё не создан)
kubectl create namespace ingress-nginx --dry-run=client -o yaml | kubectl apply -f -

# Проверить, есть ли уже релиз ingress-nginx
helm list -n ingress-nginx | grep ingress-nginx

# Если есть, удалить:
helm uninstall ingress-nginx -n ingress-nginx

# Удалить cluster-wide ресурсы, которые могут конфликтовать
kubectl delete clusterrole ingress-nginx --ignore-not-found
kubectl delete clusterrolebinding ingress-nginx --ignore-not-found
kubectl delete ingressclass nginx --ignore-not-found
kubectl delete validatingwebhookconfiguration ingress-nginx --ignore-not-found
kubectl delete validatingwebhookconfiguration ingress-nginx-admission --ignore-not-found
kubectl delete mutatingwebhookconfiguration ingress-nginx --ignore-not-found
kubectl delete mutatingwebhookconfiguration ingress-nginx-admission --ignore-not-found

#### 2.3 Установка Ingress

helm install ingress-nginx ingress-nginx/ingress-nginx \
--namespace ingress-nginx \
--set controller.metrics.enabled=true \
--set controller.metrics.serviceMonitor.enabled=true \
--set controller.metrics.serviceMonitor.additionalLabels.release="prometheus" \
--set controller.podAnnotations."prometheus\.io/scrape"="true" \
--set controller.podAnnotations."prometheus\.io/port"="10254"

#### 2.4 Проброс внешнего порта из minikube
minikube tunnel

#### 2.5 Запуск Prometheus
helm install prometheus ./user/charts/prometheus -n microservices

#### 2.6 Запуск Grafana
helm install grafana ./charts/grafana -n microservices

### 3. Запуск приложения:

#### Сборка образов:
docker build -t victor2023victorovich/billing-app:latest .
docker build -t victor2023victorovich/notification-app:latest .
docker build -t victor2023victorovich/order-app:latest .
docker build -t victor2023victorovich/user-app:latest .

#### Загрузка образов на DockerHub
docker push victor2023victorovich/billing-app:latest
docker push victor2023victorovich/notification-app:latest
docker push victor2023victorovich/order-app:latest
docker push victor2023victorovich/user-app:latest

#### Запуск микросервисов:
helm install billing ./billing/charts/billing -n microservices
helm install notification ./notification/charts/notification -n microservices
helm install order ./order/charts/order -n microservices
helm install user ./user/charts/user-app -n microservices
billing
#### Обновление микросервисов(при необходимости): 
helm upgrade --install billing ./billing/charts/billing -n microservices
helm upgrade --install notification ./notification/charts/notification -n microservices
helm upgrade --install order ./order/charts/order -n microservices
helm upgrade --install user ./user/charts/user-app -n microservices

### 4. Проверка работы:
#### Проверить поды:
kubectl get pod -n microservices -o wide

#### Проверить логи:
kubectl logs -n microservices -l app=user-app
kubectl logs -n microservices -l app=billing-app
kubectl logs -n microservices -l app=notification-app
kubectl logs -n microservices -l app=order-app

#### Проверить сервис:
kubectl get svc -n microservices

#### Проверить ingress:
kubectl get all -n ingress-nginx
kubectl get ingress -n microservices

### 5. Запуск Postman коллекции:
newman run user-app.postman_collection.json

### 6. Запуск тестов

#### Установка K6
choco install k6 -y --force

#### Запуск нагрузочных тестов
k6 run tests/load/k6/scenarios/api/test.js
