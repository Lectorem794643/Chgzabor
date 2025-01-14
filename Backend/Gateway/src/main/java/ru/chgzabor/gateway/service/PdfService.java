package ru.chgzabor.gateway.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PdfService {
    private final RestTemplate restTemplate;
    private final String pdfServiceBaseUrl = "http://pdf:8080";

    public PdfService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] generatePdf(String id, Map<String, Object> data) {
        String url = pdfServiceBaseUrl + "/pdf/model/" + id;
        return restTemplate.postForObject(url, data, byte[].class);
    }
}
