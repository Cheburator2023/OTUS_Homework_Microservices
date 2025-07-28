FROM bellsoft/liberica-openjdk-debian:21 as builder
WORKDIR /app

RUN apt-get update && apt-get install -y wget unzip && \
    wget https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip -O /tmp/gradle.zip && \
    unzip -d /opt /tmp/gradle.zip && \
    ln -s /opt/gradle-8.5/bin/gradle /usr/bin/gradle && \
    rm /tmp/gradle.zip

COPY . .
RUN gradle build --no-daemon -x test

FROM bellsoft/liberica-openjdk-debian:21
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]