package ru.chgzabor.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ImplementController {

    @GetMapping("/SlidingOptima001")
    public String showProductPage() {
        return "SlidingOptima001";
    }
}