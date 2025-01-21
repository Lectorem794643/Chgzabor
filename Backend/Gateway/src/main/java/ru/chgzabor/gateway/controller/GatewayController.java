package ru.chgzabor.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/api")
class GatewayController {
    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    private final WebClient webClient;

    @Value("${frontend.manager.base.url}")
    private String frontendManagerBaseUrl;

    @Value("${model.base.url}")
    private String modelBaseUrl;

    @Value("${pdf.base.url}")
    private String pdfBaseUrl;

    // Откатить назад, потому что 10 MB больно дохера для PDF
    public GatewayController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // Увеличиваем до 10 MB
                        .build())
                .build();
    }

    @GetMapping("/catalog")
    public ResponseEntity<String> getCatalog() {
        String serviceUrl = frontendManagerBaseUrl + "/catalog";
        try {
            String response = webClient
                    .get()
                    .uri(serviceUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (WebClientResponseException e) {
            logger.error("Error fetching catalog: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error fetching catalog", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/{id}/product")
    public ResponseEntity<String> getProduct(@PathVariable String id) {
        String serviceUrl = frontendManagerBaseUrl + "/" + id + "/product";
        try {
            String response = webClient
                    .get()
                    .uri(serviceUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (WebClientResponseException e) {
            logger.error("Error fetching product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error fetching product {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/{id}/calculating")
    public ResponseEntity<String> processCalculating(@PathVariable String id, @RequestBody String json) {
        String serviceUrl = modelBaseUrl + "/" + id + "/calculating";
        try {
            String response = webClient
                    .post()
                    .uri(serviceUrl)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (WebClientResponseException e) {
            logger.error("Error processing calculation for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error processing calculation for {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePDF(@PathVariable String id, @RequestBody Map<String, Object> data) {
        String serviceUrl = pdfBaseUrl + "/" + id + "/pdf";
        try {
            byte[] response = webClient
                    .post()
                    .uri(serviceUrl)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .body(response);
        } catch (WebClientResponseException e) {
            logger.error("Error generating PDF for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Unexpected error generating PDF for {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}