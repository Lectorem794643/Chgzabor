package ru.chgzabor.gateway.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chgzabor.gateway.service.ModelService;

import java.util.Map;

@RestController
@RequestMapping("/model")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
public class ModelController {
    private final ModelService modelService;

    @Autowired
    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping("/{id}/calculating")
    public ResponseEntity<Map<String, Object>> processData(@PathVariable String id, @RequestBody String jsonString) {
        // Получаем данные в формате JSON
        Map<String, Object> result = modelService.processData(id, jsonString);
        return ResponseEntity.ok(result);
    }
}

