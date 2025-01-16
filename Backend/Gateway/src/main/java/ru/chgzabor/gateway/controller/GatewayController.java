package ru.chgzabor.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;


@RestController
@RequestMapping("/api")
class GatewayController {
    private final WebClient.Builder webClientBuilder;

    @Value("${frontend.manager.url}")
    private String frontendManagerBaseUrl;

    @Value("${model.url}")
    private String modelBaseUrl;

    @Value("${pdf.url}")
    private String pdfBaseUrl;

    public GatewayController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/catalog")
    public ResponseEntity<String> getCatalog() {
        String serviceUrl = frontendManagerBaseUrl + "/catalog";
        String response = webClientBuilder.build()
                .get()
                .uri(serviceUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/{id}/product")
    public ResponseEntity<String> getProduct(@PathVariable String id) {
        String serviceUrl = frontendManagerBaseUrl + "/" + id + "/product";
        String response = webClientBuilder.build()
                .get()
                .uri(serviceUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/{id}/calculating")
    public ResponseEntity<String> processCalculating(@PathVariable String id, @RequestBody String json) {
        String serviceUrl = modelBaseUrl + "/" + id + "/calculating";
        String response = webClientBuilder.build()
                .post()
                .uri(serviceUrl)
                .bodyValue(json)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePDF(@PathVariable String id, @RequestBody Map<String, Object> data) {
        String serviceUrl = pdfBaseUrl + "/" + id + "/pdf";
        try {
            byte[] response = webClientBuilder.build()
                    .post()
                    .uri(serviceUrl)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
