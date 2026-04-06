# Микросервисное приложение: User / Billing / Notification / Order

Данный проект реализует систему управления пользователями, биллингом, заказами, уведомлениями, складом и доставкой 
в соответствии с заданием. Взаимодействие между сервисами построено на синхронных HTTP‑вызовах (REST).


---

## Используемый паттерн распределённой транзакции

**Saga с оркестрацией (orchestration-based saga)**  
Сервис `order` выступает оркестратором:

1. Создаётся заказ со статусом `FAILED`.
2. Последовательно вызываются шаги:
  - списание средств в `billing`
  - резервирование товара в `stock`
  - резервирование слота доставки в `delivery` (выбирается первый свободный слот на заданное время)
3. Если все шаги успешны – статус заказа меняется на `SUCCESS`.
4. При сбое любого шага выполняются **компенсирующие действия** в обратном порядке (возврат средств, освобождение товара, освобождение слота).

Этот подход обеспечивает атомарность операции создания заказа без распределённых блокировок, соответствует **SAGA**, **SOLID** (единая ответственность оркестратора) и **KISS** (явная последовательность шагов).

---

## Содержание

- [Архитектура решения](#архитектура-решения)
- [Предварительные требования](#предварительные-требования)
- [Установка](#установка)
    - [1. Установка PostgreSQL](#1-установка-postgresql)
    - [2. Установка Ingress Controller](#2-установка-ingress-controller)
    - [3. (Опционально) Prometheus и Grafana](#3-опционально-prometheus-и-grafana)
    - [4. Сборка и публикация Docker‑образов](#4-сборка-и-публикация-dockerобразов)
    - [5. Установка микросервисов](#5-установка-микросервисов)
- [Проверка работоспособности](#проверка-работоспособности)
- [Тестирование](#тестирование)
    - [Postman‑коллекция](#postmanколлекция)
    - [Результат выполнения тестов](#результат-выполнения-тестов)
- [Нагрузочное тестирование (k6)](#нагрузочное-тестирование-k6)

---

## Архитектура решения

Общая схема взаимодействия сервисов и диаграмма последовательности для сценария создания заказа представлены ниже:

| Общая архитектура | Диаграмма последовательности (создание заказа) |
|-------------------|-------------------------------------------------|
| ![Общая архитектура](user/General_architecture.png) | ![Диаграмма последовательности](user/Diagramm_sequence.png) |

**Описание взаимодействия при создании заказа:**

1. Пользователь отправляет запрос `POST /api/v1/orders` в сервис **Order**.
2. Сервис **Order** вызывает `POST /api/v1/accounts/{userId}/withdraw` в сервисе **Billing** для списания средств.
3. В зависимости от ответа Billing:
    - если списание успешно – статус заказа `SUCCESS`, иначе `FAILED`.
4. **Order** сохраняет заказ в своей БД.
5. **Order** отправляет уведомление в сервис **Notification** (`POST /api/v1/notifications`).
6. **Notification** сохраняет уведомление в своей БД и возвращает статус.

Все сервисы используют общую базу данных PostgreSQL (отдельные схемы/базы), развёрнутую в кластере Kubernetes.

---

## Предварительные требования

- Установленный [Minikube](https://minikube.sigs.k8s.io/docs/start/) или рабочее Kubernetes‑окружение.
- Установленный [Helm](https://helm.sh/docs/intro/install/) (версия 3.x).
- Установленный `kubectl`.
- Для сборки образов – Docker.
- Для тестирования – [Newman](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/) (или Postman) и [k6](https://k6.io/docs/get-started/installation/).

Убедитесь, что Minikube запущен:
```bash
minikube start
```

## Установка
Все компоненты устанавливаются в namespace microservices.
Ingress‑контроллер размещается в namespace ingress-nginx.

### 1. Установка PostgreSQL
Добавим репозиторий Bitnami и установим PostgreSQL без постоянного хранилища (для тестового окружения).

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```
Обновим при необходимости
```bash
helm repo update
```
Создаем нэймспэйс
```bash
kubectl create namespace microservices
```
Установка PostgreSQL
```bash
helm install postgres bitnami/postgresql -n microservices \
  --set auth.database=user_db \
  --set auth.username=postgres \
  --set auth.password=postgres \
  --set primary.persistence.enabled=false \
  --set volumePermissions.enabled=true
```

Дождёмся, пока под PostgreSQL перейдёт в состояние Running:

```bash
kubectl get pods -n microservices -w
```

После этого создадим дополнительные базы данных для сервисов billing, notification и order:

```bash
# Получим имя пода PostgreSQL
POD_NAME=$(kubectl get pods -n microservices -l app.kubernetes.io/name=postgresql -o jsonpath="{.items[0].metadata.name}")

# Подключимся к поду и выполним SQL
kubectl exec -n microservices -it $POD_NAME -- bash -c "psql -U postgres <<EOF
CREATE DATABASE billing_db;
CREATE DATABASE notification_db;
CREATE DATABASE order_db;
CREATE DATABASE stock_db;
CREATE DATABASE delivery_db;
EOF"
```

### 2. Установка Ingress Controller
Установим NGINX Ingress Controller с включёнными метриками для Prometheus.

```bash
# Добавим репозиторий ingress-nginx
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
```

```bash
# Обновим при необходимости
helm repo update
```

### Создадим namespace
```bash
kubectl create namespace ingress-nginx --dry-run=client -o yaml | kubectl apply -f -
```
```bash
# Проверим, есть ли уже релиз ingress-nginx
helm list -n ingress-nginx | grep ingress-nginx
```
```bash
# Если есть, удалить:
helm uninstall ingress-nginx -n ingress-nginx
```

# Удалим cluster-wide ресурсы, которые могут конфликтовать
```bash
kubectl delete clusterrole ingress-nginx --ignore-not-found
```
```bash
kubectl delete clusterrolebinding ingress-nginx --ignore-not-found
```
```bash
kubectl delete ingressclass nginx --ignore-not-found
```
```bash
kubectl delete validatingwebhookconfiguration ingress-nginx --ignore-not-found
```
```bash
kubectl delete validatingwebhookconfiguration ingress-nginx-admission --ignore-not-found
```
```bash
kubectl delete mutatingwebhookconfiguration ingress-nginx --ignore-not-found
```
```bash
kubectl delete mutatingwebhookconfiguration ingress-nginx-admission --ignore-not-found
```

### Установим ingress-nginx
```bash
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --set controller.metrics.enabled=true \
  --set controller.metrics.serviceMonitor.enabled=true \
  --set controller.metrics.serviceMonitor.additionalLabels.release="prometheus" \
  --set controller.podAnnotations."prometheus\.io/scrape"="true" \
  --set controller.podAnnotations."prometheus\.io/port"="10254"
```

Если вы используете Minikube, пробросьте туннель, чтобы получить внешний IP для ingress:

```bash
minikube tunnel
```

### 3. Prometheus и Grafana
Для мониторинга нужно развернуть Prometheus и Grafana из чартов, находящихся в директории user/charts.

```bash
# Установка Prometheus
helm install prometheus ./user/charts/prometheus -n microservices
```

```bash
# Установка Grafana
helm install grafana ./user/charts/grafana -n microservices
```

После установки Grafana будет доступна по адресу http://grafana.arch.homework (если настроен ingress). 
Логин/пароль по умолчанию: admin / admin (при первом входе потребуется сменить).

### 4. Сборка и публикация Docker‑образов
Пересоберите образы микросервисов и загрузите их на Docker Hub (или другой registry).

```bash
# Сборка
docker build --no-cache -t you_registry/user-app:latest ./user
docker build --no-cache -t you_registry/billing-app:latest ./billing
docker build --no-cache -t you_registry/notification-app:latest ./notification
docker build --no-cache -t you_registry/order-app:latest ./order
docker build --no-cache -t you_registry/stock-app:latest ./stock
docker build --no-cache -t you_registry/delivery-app:latest ./delivery
```
Можно сразу скачать готовые
```bash
# Публикация
docker push victor2023victorovich/user-app:latest
docker push victor2023victorovich/billing-app:latest
docker push victor2023victorovich/notification-app:latest
docker push victor2023victorovich/order-app:latest
docker push victor2023victorovich/stock-app:latest


```

### 5. Установка микросервисов
Установите каждый сервис с помощью Helm, используя подготовленные чарты.
Важно: перед установкой убедитесь, что в файлах values.yaml всех сервисов указаны корректные ссылки на образы (ваш registry) 
и параметры подключения к БД (они уже настроены на использование сервиса postgres-postgresql внутри namespace microservices).

```bash
# Установка сервиса пользователей
helm install user ./user/charts/user-app -n microservices
```
```bash
# Установка биллинга
helm install billing ./billing/charts/billing -n microservices
```
```bash
# Установка уведомлений
helm install notification ./notification/charts/notification -n microservices
```
```bash
# Установка заказов
helm install order ./order/charts/order -n microservices
```
```bash
helm install stock ./stock/charts/stock -n microservices
```
```bash
helm install delivery ./delivery/charts/delivery -n microservices
```

При необходимости обновления используйте helm upgrade --install ... с теми же параметрами.

## Проверка работоспособности
После установки проверьте состояние подов и сервисов:

```bash
kubectl get pods -n microservices
```
```bash
kubectl get svc -n microservices
```
```bash
kubectl get ingress -n ingress-nginx
```

Все поды должны быть в статусе Running.
Логи можно посмотреть командой:

```bash
kubectl logs -n microservices -l app=user-app --tail=50 -f
```
```bash
kubectl logs -n microservices -l app=billing-app --tail=50 -f
```
```bash
kubectl logs -n microservices -l app=delivery --tail=50 -f
```
```bash
kubectl logs -n microservices -l app=notification-app --tail=50 -f
```
```bash
kubectl logs -n microservices -l app=order-app --tail=50 -f
```
```bash
kubectl logs -n microservices -l app=stock-app --tail=50 -f
```

Проверьте доступность приложений через ingress.
Добавьте в файл hosts (или используйте minikube tunnel) следующие домены:

arch.homework → user‑service

billing.arch.homework → billing‑service

notify.arch.homework → notification‑service

order.arch.homework → order‑service

delivery.arch.homework → delivery‑service

stock.arch.homework → stock‑service

Пример проверки через curl:

```bash
curl http://arch.homework/api/v1/users
```

## Тестирование
### Postman‑коллекция
Для функционального тестирования подготовлена коллекция Postman, покрывающая полный сценарий:

создание пользователя

пополнение счёта

создание заказа с достаточными средствами

проверка баланса и уведомления

создание заказа с недостаточными средствами

повторная проверка баланса и уведомления

Коллекция находится в файле user-billing-order-notification.postman_collection.json.
Переменная окружения baseUrl по умолчанию указывает на http://arch.homework.

Для функционального тестирования сервисов доставки и склада подготовлена отдельная коллекция Postman, 
покрывающая сценарий тестирования Saga:

создание заказа с достаточными средствами (успех)

создание заказа с недостаточными средствами (отказ)

создание заказа с несуществующим товаром (отказ)

проверка баланса и уведомлений

Коллекция находится в файле test-saga.json.
Переменная окружения baseUrl по умолчанию указывает на http://arch.homework.

### Результат выполнения тестов
Запустите коллекции с помощью Newman. Последовательно, сначала:

```bash
newman run user-billing-order-notification.postman_collection.json
```
затем:

```bash
newman run user-billing-order-notification.postman_collection.json
```
Ниже представлен скриншот успешного прохождения всех тестов:

![Результаты выполнения тестов](postman-tests.png)

![Результаты выполнения тестов saga](Test_saga.png)

## Заключение
Все сервисы успешно разворачиваются в Kubernetes, взаимодействуют через HTTP, проходят функциональные тесты
