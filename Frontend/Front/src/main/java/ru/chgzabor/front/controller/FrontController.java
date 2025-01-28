package ru.chgzabor.front.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class FrontController {

    private static final String NOT_FOUND_PAGE = "static/error/error.html";

    /**
     * Универсальный метод для возврата HTML-страниц.
     *
     * @param path     путь к ресурсу
     * @param response HTTP-ответ
     */
    private void serveResource(String path, HttpServletResponse response) throws IOException {
        Resource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            handleResourceNotFound(response);
            return;
        }

        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }

    /**
     * Обработка отсутствующего ресурса.
     *
     * @param response HTTP-ответ
     */
    private void handleResourceNotFound(HttpServletResponse response) throws IOException {
        Resource notFoundResource = new ClassPathResource(NOT_FOUND_PAGE);

        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType("text/html; charset=UTF-8");

        if (notFoundResource.exists()) {
            StreamUtils.copy(notFoundResource.getInputStream(), response.getOutputStream());
        } else {
            response.getWriter().write("<h1>Resource not found</h1>");
        }
    }

    @GetMapping("/")
    public void showHomePage(HttpServletResponse response) throws IOException {
        serveResource("static/catalog/catalog.html", response);
    }

    @GetMapping("/catalog")
    public void showCatalogPage(HttpServletResponse response) throws IOException {
        serveResource("static/catalog/catalog.html", response);
    }

    @GetMapping("/manual")
    public void showManualPage(HttpServletResponse response) throws IOException {
        serveResource("static/manual/manual.html", response);
    }

    @GetMapping("/product/{id}")
    public void showProductPage(@PathVariable String id, HttpServletResponse response) throws IOException {
        serveResource("static/products/" + id + ".html", response);
    }

    @GetMapping("/error-page")
    public void showErrorPage(HttpServletResponse response) throws IOException {
        serveResource(NOT_FOUND_PAGE, response);
    }
}
