package ru.chgzabor.pdf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.chgzabor.pdf.service.PageContentGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
    @PostMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePDF(@PathVariable String id, @RequestBody Map<String, Object> data) {
        try {
            // Динамическая загрузка класса
            Class<?> clazz = Class.forName("ru.chgzabor.pdf.implement." + id);
            Constructor<?> constructor = clazz.getConstructor();

            PageContentGenerator generator = (PageContentGenerator) constructor.newInstance();

            // Вызов метода генерации PDF
            Method method = clazz.getMethod("generatePDF", Map.class);
            byte[] pdfBytes = (byte[]) method.invoke(generator, data);

            // Подготовка ответа с PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", id + ".pdf");
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (ClassNotFoundException e) {
            logger.error("Class not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (NoSuchMethodException e) {
            logger.error("Method not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (InvocationTargetException e) {
            logger.error("Error invoking method: " + e.getCause().getMessage(), e.getCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error invoking method: " + e.getCause().getMessage()).getBytes());
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

