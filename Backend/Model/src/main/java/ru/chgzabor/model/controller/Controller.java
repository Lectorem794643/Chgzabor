package ru.chgzabor.model.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chgzabor.model.dto.GlobalDTO;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@RestController
public class Controller {
    @CrossOrigin(origins = "http://localhost:8000", allowedHeaders = "*")
    @PostMapping("/{id}/calculating")
    public ResponseEntity<GlobalDTO> processData(@PathVariable String id, @RequestBody String jsonString) {
        try {
            GlobalDTO dataModel = GlobalDTO.fromJsonString(jsonString); // Преобразование JSON в GlobalDTO
            Class<?> clazz = Class.forName("ru.chgzabor.model.implement." + id);
            Constructor<?> constructor = clazz.getConstructor(GlobalDTO.class);
            Object instance = constructor.newInstance(dataModel);
            Method method = clazz.getMethod("calculatModel");
            GlobalDTO result = (GlobalDTO) method.invoke(instance);

            return ResponseEntity.ok(result);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}