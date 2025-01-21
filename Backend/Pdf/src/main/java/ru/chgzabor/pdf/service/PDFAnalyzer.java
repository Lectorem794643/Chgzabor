package ru.chgzabor.pdf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


@Service
public class PDFAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(PDFAnalyzer.class);

    public String analyzeToJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("Starting analysis for PDF file: {}", filePath);

        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            logger.debug("Successfully loaded PDF document: {}", filePath);
            PDFInfo pdfInfo = new PDFInfo();

            // Размеры документа
            PDPage firstPage = document.getPage(0);
            float width = firstPage.getMediaBox().getWidth();
            float height = firstPage.getMediaBox().getHeight();
            pdfInfo.setWidth(width);
            pdfInfo.setHeight(height);
            logger.info("PDF dimensions - Width: {}, Height: {}", width, height);

            // Подсчет символов
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            logger.debug("Extracted text from PDF document.");

            int cyrillicCount = 0;
            int latinCount = 0;
            for (char ch : text.toCharArray()) {
                if (Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.CYRILLIC) {
                    cyrillicCount++;
                } else if (Character.isAlphabetic(ch) && Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.BASIC_LATIN) {
                    latinCount++;
                }
            }
            pdfInfo.setCyrillicCharacterCount(cyrillicCount);
            pdfInfo.setLatinCharacterCount(latinCount);
            logger.info("Character count - Cyrillic: {}, Latin: {}", cyrillicCount, latinCount);

            // Сбор шрифтов и проверка на подмножество
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources != null) {
                    for (COSName fontName : resources.getFontNames()) {
                        PDFont font = resources.getFont(fontName);
                        if (font != null) {
                            boolean isSubset = font.getName().contains("+");
                            pdfInfo.getFonts().put(font.getName(), isSubset);
                            logger.debug("Font found: {}, Subset: {}", font.getName(), isSubset);
                        }
                    }
                }
            }

            // Конвертация в JSON
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pdfInfo);
            logger.info("Successfully analyzed PDF document. Returning JSON result.");
            return jsonResult;

        } catch (IOException e) {
            logger.error("Error analyzing PDF: {}", e.getMessage(), e);
            return "{\"error\":\"Failed to analyze PDF\"}";
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PDFInfo {
        private float width;
        private float height;
        private Map<String, Boolean> fonts = new HashMap<>();
        private int cyrillicCharacterCount;
        private int latinCharacterCount;
    }
}
