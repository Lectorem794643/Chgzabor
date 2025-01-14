# Используем базовый образ с JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл микросервиса (сборка через Maven/Gradle)
COPY target/Gateway-0.0.1.jar gateway.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "gateway.jar"]
