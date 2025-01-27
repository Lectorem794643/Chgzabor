package ru.chgzabor.front.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(Exception.class)
    public void handleException(HttpServletResponse response) throws IOException {
        var resource = new ClassPathResource("static/error/error.html");
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType("text/html; charset=UTF-8");
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }
}


