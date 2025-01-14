package ru.chgzabor.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chgzabor.gateway.service.FrontendService;

import java.io.IOException;


@RestController
@RequestMapping("/api")
public class FrontendController {

    private final FrontendService frontendService;

    @Autowired
    public FrontendController(FrontendService frontendService) {
        this.frontendService = frontendService;
    }

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
    @GetMapping("/catalog")
    public ResponseEntity<Resource> getCatalog() throws IOException {
        return frontendService.getCatalog();
    }

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
    @GetMapping("/{id}/product")
    public ResponseEntity<Resource> getProduct(@PathVariable String id) throws IOException {
        return frontendService.getProduct(id);
    }
}

