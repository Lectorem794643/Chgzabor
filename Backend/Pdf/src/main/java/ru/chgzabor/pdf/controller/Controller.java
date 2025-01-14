package ru.chgzabor.pdf.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chgzabor.pdf.service.PdfGenerator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

@RestController
public class Controller {
    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
    @PostMapping("/model/{id}/pdf")
    public ResponseEntity<byte[]> generatePDF(@PathVariable String id, @RequestBody Map<String, Object> data) throws IOException {
        try {
            Class<?> clazz = Class.forName("ru.chgzabor.pdf.implement." + id);
            Constructor<?> constructor = clazz.getConstructor(String.class);

            String inputFilePath = "drawing/" + id + ".pdf";
            PdfGenerator generator = (PdfGenerator) constructor.newInstance(inputFilePath);

            Method method = clazz.getMethod("generatePDF", Map.class);
            byte[] pdfBytes = (byte[]) method.invoke(generator, data);

            // Подготовка ответа с PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
