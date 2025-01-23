package ru.chgzabor.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String getHome() {
        return "catalog";
    }

    @GetMapping("/catalog")
    public String getCatalog() {
        return "catalog";
    }

    @GetMapping("/not-found")
    public String getNotFound() {
        return "not-found";  // Ваша страница каталога
    }
}
