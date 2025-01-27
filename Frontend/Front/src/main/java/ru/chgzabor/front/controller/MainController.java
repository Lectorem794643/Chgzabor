package ru.chgzabor.front.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class MainController {
    @GetMapping("/")
    public void showHomePage(HttpServletResponse response) throws IOException {
        var resource = new ClassPathResource("static/catalog/catalog.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }

    @GetMapping("/catalog")
    public void showCatalogPage(HttpServletResponse response) throws IOException {
        var resource = new ClassPathResource("static/catalog/catalog.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }

    @GetMapping("/manual")
    public void showManualPage(HttpServletResponse response) throws IOException {
        var resource = new ClassPathResource("static/manual/manual.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }

    @GetMapping("/error-page")
    public void showErrorPage(HttpServletResponse response) throws IOException {
        var resource = new ClassPathResource("static/error/error.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }
}
