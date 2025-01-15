package ru.chgzabor.frontendmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Cacheable("catalogCache")
    public String loadCatalog() throws IOException {
        try (InputStream inputStream = new ClassPathResource("react-config/catalog.json").getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Cacheable("productCache")
    public String loadProduct(String id) throws IOException {
        try (InputStream inputStream = new ClassPathResource("products/" + id + ".json").getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // Метод, который будет загружать файлы при старте приложения
    @PostConstruct
    public void init() {
        try {
            loadCatalog(); logger.info("Catalog file loaded into cache.");

            // Загружаем в кеш все конфиги продуктов
            loadProduct("SlidingOptima001");

            logger.info("All product file loaded into cache.");
        } catch (IOException e) {
            logger.error("Error loading files into cache: {}", e.getMessage());
        }
    }
}