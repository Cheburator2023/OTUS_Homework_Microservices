## Инструкции по запуску и тестированию:

### 1. Установка БД из Helm:
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres stable/postgresql -n user-app \
--set postgresqlDatabase=user_db \
--set postgresqlUsername=postgres \
--set postgresqlPassword=postgres \
--set persistence.enabled=false

### 2. Применение первоначальных миграций:
kubectl apply -f user/charts/user-app/templates/migration-job.yaml

### 3. Запуск приложения:

#### Сборка образа:
docker build -t user-app .

#### Установка Helm чарта:
helm install user-app ./user/charts/user-app

#### Применение всех манифестов в правильном порядке:
kubectl apply -f user/charts/user-app/templates/configmap.yaml
kubectl apply -f user/charts/user-app/templates/secret.yaml
kubectl apply -f user/charts/user-app/templates/service.yaml
kubectl apply -f user/charts/user-app/templates/deployment.yaml
kubectl apply -f user/charts/user-app/templates/ingress.yaml

### 4. Проверка работы:
#### Проверить поды:
kubectl get pods

#### Проверить логи:
kubectl logs -f <pod-name>

#### Проверить сервис:
kubectl get svc

#### Проверить ingress:
kubectl get ingress

### 5. Запуск Postman коллекции:
newman run user-app.postman_collection.json