package ru.chgzabor.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/dev")
public class DevController {
    private static final Logger logger = LoggerFactory.getLogger(DevController.class);
    private final WebClient webClient;

    @Value("${pdf.base.url}")
    private String pdfBaseUrl;

    // Откатить назад, потому что 10 MB больно дохера для PDF
    public DevController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // Увеличиваем до 10 MB
                        .build())
                .build();
    }

    // Получаем информацию о PDF
    @GetMapping("/pdf/{id}/info")
    public ResponseEntity<String> getPDFInfo(@PathVariable String id) {
        logger.info("Received request to get PDF info for id: {}", id);

        try {
            // Отправляем запрос к сервису PDF с использованием id
            String pdfInfo = webClient.get()
                    .uri(pdfBaseUrl + "/{id}/info", id)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Синхронный вызов

            logger.info("PDF info retrieved successfully for id: {}", id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pdfInfo);
        } catch (Exception e) {
            logger.error("Failed to retrieve PDF info for id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Failed to retrieve PDF info\"}");
        }
    }
}
