package ru.chgzabor.frontendmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.chgzabor.frontendmanager.service.FileService;

import java.io.IOException;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileService fileService;

    @GetMapping("/catalog")
    public ResponseEntity<String> getCatalog() throws IOException {
        String catalog = fileService.loadCatalog();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(catalog);
    }

    @GetMapping("/{id}/product")
    public ResponseEntity<String> getProduct(@PathVariable String id) throws IOException {
        String product = fileService.loadProduct(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(product);
    }
}
