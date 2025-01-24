package ru.chgzabor.front.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/product")
public class ProductController {
    @GetMapping("/SlidingOptima001")
    public void showProductPage(HttpServletResponse response) throws IOException {
        var resource = new ClassPathResource("static/products/SlidingOptima001.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
    }
}
