package ru.chgzabor.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ModelService {

    private final RestTemplate restTemplate;

    @Value("${MODEL_SERVICE_URL:http://model:8080}")
    private String modelServiceUrl;

    @Autowired
    public ModelService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Map<String, Object> processData(String id, String jsonString) {
        // Формируем URL для обращения к сервису model
        String url = String.format("%s/model/%s/calculating", modelServiceUrl, id);

        // Отправляем запрос и получаем ответ как Map
        ResponseEntity<Map> response = restTemplate.postForEntity(url, jsonString, Map.class);

        // Возвращаем тело ответа, которое будет в формате Map
        return response.getBody();
    }
}

