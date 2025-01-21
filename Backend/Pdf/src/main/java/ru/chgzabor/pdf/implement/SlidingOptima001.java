package ru.chgzabor.pdf.implement;

import ru.chgzabor.pdf.service.PageContentGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlidingOptima001 extends PageContentGenerator {
    public byte[] generatePDF(Map<String, Object> dto) throws IOException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("L", String.valueOf(dto.get("L")));
        replacements.put("H", String.valueOf(dto.get("H")));
        replacements.put("B", String.valueOf(dto.get("B")));
        replacements.put("E", String.valueOf(dto.get("E")));
        replacements.put("C", String.valueOf(dto.get("C")));
        replacements.put("D", String.valueOf(dto.get("D")));
        replacements.put("A", String.valueOf(dto.get("A")));

        return run("SlidingOptima001", replacements, dto);
    }
}

// Можно переопределить функции заполнения контентом первой и второй страницы

//    @Override
//    protected void addContentToFirstPage(PDDocument document, PDPage page, Map<String, String> dto) throws IOException {
//
//    }
//
//    @Override
//    protected void addContentToSecondPage(PDDocument document, PDPage page, Map<String, Object> dto) throws IOException {
//
//    }