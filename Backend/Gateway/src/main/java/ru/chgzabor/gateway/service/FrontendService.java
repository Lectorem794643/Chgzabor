package ru.chgzabor.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class FrontendService {

    private final RestTemplate restTemplate;

    @Value("${FRONTEND_MANAGER_URL:http://frontend-manager:8080}")
    private String frontendManagerUrl;

    @Autowired
    public FrontendService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // Проксируем запрос на получение каталога
    public ResponseEntity<Resource> getCatalog() throws IOException {
        String url = frontendManagerUrl + "/api/catalog";  // URL на frontend-manager
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
        return response; // Возвращаем ответ без изменений
    }

    // Проксируем запрос на получение продукта
    public ResponseEntity<Resource> getProduct(String id) throws IOException {
        String url = frontendManagerUrl + "/api/" + id + "/product";  // URL на frontend-manager
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
        return response; // Возвращаем ответ без изменений
    }
}


