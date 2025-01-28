package ru.chgzabor.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
public class StaticController {

    private static final Logger logger = LoggerFactory.getLogger(StaticController.class);

    private final WebClient webClient;

    // Инъекция URL для базы фронтенда
    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    // Конструктор для инъекции WebClient
    public StaticController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(frontendBaseUrl)  // Используем frontendBaseUrl для базового URL
                .build();
    }

    // Прокси для статических файлов, с параметром 'file'
    @GetMapping("/static")
    public void proxyStaticFile(@RequestParam String file, HttpServletResponse response) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.error("Путь к файлу не был передан!");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("<h1>File path is missing</h1>");
            return;
        }

        logger.info("Запрос на статический файл: {}", file);

        try {
            // Выполняем HTTP-запрос через WebClient
            Resource resource = webClient.get()
                    .uri(frontendBaseUrl + file) // передаем путь как часть запроса
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        logger.error("Ошибка при получении файла: {}. Статус: {}", file, clientResponse.statusCode());
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
                    })
                    .bodyToMono(Resource.class)
                    .block(); // Используем block() для синхронного получения результата

            if (resource != null) {
                logger.info("Файл {} найден, отправляем в ответ", file);

                // Проверяем расширение файла и устанавливаем правильный MIME-тип
                if (file.endsWith(".svg")) {
                    response.setContentType("image/svg+xml");
                } else if (file.endsWith(".css")) {
                    response.setContentType("text/css");
                } else if (file.endsWith(".js")) {
                    response.setContentType("application/javascript");
                } else if (file.endsWith(".woff2")) {
                    response.setContentType("font/woff2");
                } else if (file.endsWith(".woff")) {
                    response.setContentType("font/woff");
                } else if (file.endsWith(".ttf")) {
                    response.setContentType("font/ttf");
                } else if (file.endsWith(".eot")) {
                    response.setContentType("application/vnd.ms-fontobject");
                } else if (file.endsWith(".otf")) {
                    response.setContentType("font/otf");
                } else if (file.endsWith(".jpg") || file.endsWith(".jpeg")) {
                    response.setContentType("image/jpeg");
                } else if (file.endsWith(".png")) {
                    response.setContentType("image/png");
                } else if (file.endsWith(".gif")) {
                    response.setContentType("image/gif");
                } else {
                    response.setContentType("application/octet-stream");
                }

                // Устанавливаем дополнительные заголовки
                response.setHeader("Cache-Control", "public, max-age=86400"); // Кэширование на 1 день
                response.setHeader("Expires", Instant.now().plus(1, ChronoUnit.DAYS).toString()); // Устанавливаем время истечения кэша
                response.setHeader("Pragma", "cache");

                // Передаем файл
                response.setCharacterEncoding("UTF-8");
                resource.getInputStream().transferTo(response.getOutputStream());
            } else {
                logger.warn("Файл {} не найден", file);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }

        } catch (Exception e) {
            logger.error("Ошибка при проксировании файла: {}", file, e);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("<h1>File not found: " + file + "</h1>");
        }
    }
}
