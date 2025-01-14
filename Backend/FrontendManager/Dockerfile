# Используем базовый образ с JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл микросервиса (сборка через Maven/Gradle)
COPY target/FrontendManager-0.0.1.jar frontend-manager.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "frontend-manager.jar"]
