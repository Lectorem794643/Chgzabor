package ru.chgzabor.frontendmanager.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
    @GetMapping("/api/catalog")
    public ResponseEntity<Resource> getCatalog() throws IOException {
        // Чтение каталога JSON-файла
        Resource catalogFile = new ClassPathResource("catalog.json");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(catalogFile);
    }

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
    @GetMapping("/api/{id}/product")
    public ResponseEntity<Resource> getProduct(@PathVariable String id) throws IOException {
        // Проверка на существование продукта
        if (!"SlidingOptima001".equals(id)) {
            // Возвращаем ошибку с кодом 404 и текстом ошибки
            String errorMessage = "Product with id " + id + " not found";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new ByteArrayResource(errorMessage.getBytes()));
        }

        // Чтение продукта JSON-файла
        Resource productFile = new ClassPathResource("product.json");

        // Возвращаем файл с типом контента application/json
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productFile);
    }
}
