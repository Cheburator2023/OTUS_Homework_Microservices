# ---- Этап сборки ----
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Копируем только файлы, необходимые для загрузки зависимостей
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Даём права на выполнение gradlew
RUN chmod +x gradlew

# Загружаем зависимости (кэшируем этот слой)
RUN ./gradlew dependencies --no-daemon

# Теперь копируем остальной исходный код
COPY src src

# Собираем приложение (без тестов для ускорения)
RUN ./gradlew clean build --no-daemon -x test

# ---- Финальный образ ----
FROM bellsoft/liberica-openjdk-debian:21

# Создаём непривилегированного пользователя
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --gid 1001 appuser

WORKDIR /app

# Копируем JAR-файл из этапа сборки
COPY --from=builder /app/build/libs/*.jar app.jar

# Указываем порт, который слушает приложение (из application.yml)
EXPOSE 8004

# Переключаемся на непривилегированного пользователя
USER appuser

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]