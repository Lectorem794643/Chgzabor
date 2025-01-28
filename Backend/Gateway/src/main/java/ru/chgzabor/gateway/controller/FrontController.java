package ru.chgzabor.gateway.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
public class FrontController {
    private static final Logger logger = LoggerFactory.getLogger(FrontController.class);

    private final WebClient webClient;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    public FrontController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .build();
    }

    @GetMapping("/")
    public ResponseEntity<String> proxyHomePage() {
        return proxyRequest("/");
    }

    @GetMapping("/catalog")
    public ResponseEntity<String> proxyCatalogPage() {
        return proxyRequest("/catalog");
    }

    @GetMapping("/manual")
    public ResponseEntity<String> proxyManualPage() {
        return proxyRequest("/manual");
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<String> proxyProductPage(@PathVariable String id) {
        return proxyRequest("/product/" + id);
    }

    @GetMapping("/error-page")
    public ResponseEntity<String> proxyErrorPage() {
        return proxyRequest("/error-page");
    }

    /**
     * Универсальный метод проксирования запросов.
     *
     * @param endpoint конечная точка на фронтенде
     * @return ответ от фронтенда
     */
    private ResponseEntity<String> proxyRequest(String endpoint) {
        String targetUrl = frontendBaseUrl + endpoint;
        try {
            String response = webClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(response);
        } catch (WebClientResponseException e) {
            logger.error("Error proxying request to {}: {}", targetUrl, e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error proxying request to {}", targetUrl, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}
