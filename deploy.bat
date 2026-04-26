@echo off
setlocal enabledelayedexpansion

:: ====================================================
::   Online Cafe Microservices Deployment Script
:: ====================================================

set NAMESPACE=microservices
set INGRESS_NS=ingress-nginx
set POSTGRES_HELM_RELEASE=postgres
set POSTGRES_PASSWORD=postgres
set DOCKER_REGISTRY=victor2023victorovich

echo ====================================================
echo   Online Cafe - Deployment Script
echo ====================================================
echo.

:: 1. Проверка инструментов
echo [1] Проверка инструментов...
where minikube >nul 2>nul || (echo Minikube not found & exit /b 1)
where helm >nul 2>nul || (echo Helm not found & exit /b 1)
where kubectl >nul 2>nul || (echo kubectl not found & exit /b 1)
echo Инструменты найдены.
echo.

:: 2. Запуск Minikube
echo [2] Запуск Minikube...
minikube status | findstr "host: Running" >nul
if errorlevel 1 (
    echo Minikube не запущен, запускаем...
    minikube start --driver=docker --cpus=2 --memory=6144 --disk-size=40g
    if errorlevel 1 exit /b 1
) else (
    echo Minikube уже запущен.
)
echo.

:: 3. Создание namespace
echo [3] Создание namespace...
kubectl create namespace %NAMESPACE% --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace %INGRESS_NS% --dry-run=client -o yaml | kubectl apply -f -
echo.

:: 4. Установка PostgreSQL
echo [4] Установка PostgreSQL через Helm...
helm list -n %NAMESPACE% | findstr %POSTGRES_HELM_RELEASE% >nul
if errorlevel 1 (
    helm repo add bitnami https://charts.bitnami.com/bitnami --force-update 2>nul
    helm repo update 2>nul
    helm install %POSTGRES_HELM_RELEASE% bitnami/postgresql -n %NAMESPACE% ^
        --set auth.database=user_db ^
        --set auth.username=postgres ^
        --set auth.password=%POSTGRES_PASSWORD% ^
        --set primary.persistence.enabled=false ^
        --set volumePermissions.enabled=true
) else (
    echo PostgreSQL уже установлен.
)

:: Ожидание готовности PostgreSQL
echo Ожидание готовности PostgreSQL...
set attempt=0
:wait_postgres
set /a attempt+=1
kubectl get pod -l app.kubernetes.io/name=postgresql -n %NAMESPACE% -o name >nul 2>nul
if errorlevel 1 (
    if !attempt! lss 15 (
        timeout /t 5 /nobreak >nul
        goto wait_postgres
    ) else (
        echo Ошибка: под PostgreSQL не появился.
        exit /b 1
    )
)
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=postgresql -n %NAMESPACE% --timeout=120s
if errorlevel 1 (
    echo Ошибка: PostgreSQL не готов.
    exit /b 1
)
echo PostgreSQL готов.
echo.

:: 5. Создание дополнительных баз данных
echo [5] Создание баз данных для сервисов...
set POD_NAME=
for /f "tokens=1" %%i in ('kubectl get pods -n %NAMESPACE% 2^>nul ^| findstr "postgresql" ^| findstr "Running"') do (
    set POD_NAME=%%i
    goto :found
)
:found
if "%POD_NAME%"=="" (
    echo Не удалось получить имя пода PostgreSQL.
    exit /b 1
)
echo Используется под: %POD_NAME%

for %%b in (billing_db notification_db order_db stock_db delivery_db) do (
    echo Создание базы %%b...
    kubectl exec -n %NAMESPACE% %POD_NAME% -- sh -c "PGPASSWORD=%POSTGRES_PASSWORD% psql -U postgres -tc \"SELECT 1 FROM pg_database WHERE datname='%%b'\" | grep -q 1 || PGPASSWORD=%POSTGRES_PASSWORD% psql -U postgres -c \"CREATE DATABASE %%b\"" --request-timeout=30s >nul 2>&1
    if errorlevel 1 (
        echo Ошибка при создании %%b
    ) else (
        echo База %%b создана или уже существует
    )
)
echo Базы данных готовы.
echo.

:: 6. Установка Ingress Controller (ServiceMonitor отключён)
echo [6] Установка Ingress Controller...
helm list -n %INGRESS_NS% | findstr ingress-nginx >nul
if errorlevel 1 (
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx --force-update
    helm repo update
    helm install ingress-nginx ingress-nginx/ingress-nginx ^
        --namespace %INGRESS_NS% ^
        --set controller.metrics.enabled=true ^
        --set controller.metrics.serviceMonitor.enabled=false ^
        --set controller.podAnnotations."prometheus\.io/scrape"="true" ^
        --set controller.podAnnotations."prometheus\.io/port"="10254"
    echo Ожидание готовности ingress-nginx...
    kubectl wait --namespace %INGRESS_NS% --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s
) else (
    echo Ingress Controller уже установлен.
)
echo.

:: 7. Сборка образов (пропуск)
echo [7] Сборка Docker образов (пропускаем, используем готовые: %DOCKER_REGISTRY%/*-app:latest)
echo.

:: 8. Установка микросервисов (отключаем мониторинг)
echo [8] Установка микросервисов...
helm upgrade --install user ./user/charts/user-app -n %NAMESPACE% --wait --set monitoring.enabled=false
helm upgrade --install billing ./billing/charts/billing -n %NAMESPACE% --wait
helm upgrade --install notification ./notification/charts/notification -n %NAMESPACE% --wait
helm upgrade --install stock ./stock/charts/stock -n %NAMESPACE% --wait
helm upgrade --install delivery ./delivery/charts/delivery -n %NAMESPACE% --wait
helm upgrade --install order ./order/charts/order -n %NAMESPACE% --wait
echo Все сервисы установлены.
echo.

:: 9. Ожидание готовности подов
echo [9] Ожидание готовности всех подов в namespace %NAMESPACE%...
kubectl wait --for=condition=ready pod --all -n %NAMESPACE% --timeout=180s
echo.

:: 10. Вывод информации
echo ====================================================
echo   Развертывание завершено!
echo ====================================================
echo.
echo Проверьте статус подов: kubectl get pods -n %NAMESPACE%
echo Проверьте ingress: kubectl get ingress -n %INGRESS_NS%
echo Запустите в отдельной консоле: minikube tunnel
echo.
echo Для тестирования выполните:
echo   newman run e2e-tests.json -e postman_environment.json
echo.
pause