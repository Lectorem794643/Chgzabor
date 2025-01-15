package ru.chgzabor.pdf.service;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PdfGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);
    private final String inputFilePath;
    private static PDType0Font FONT;

    float yPosition; // Костыль чутка

    private static final float RECTANGLE_LINE_WIDTH = 1;
    private static final float PAGE_WIDTH = 842;
    private static final float PAGE_HEIGHT = 595;
    private static final float PAGE_INDENT = 40;
    private static final float LINE_SPACING = 20;
    private static final float FONT_SIZE = 14;
    private static final String FONT_PATH = "font/ArialCyr.TTF";

    public PdfGenerator(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public byte[] run(Map<String, String> replacements, Map<String, Object> dto) throws IOException {
        try (PDDocument document = new PDDocument()) {
            logger.info("Starting PDF generation.");
            loadFont();

            PDPage firstPage = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            PDPage secondPage = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            document.addPage(firstPage);
            document.addPage(secondPage);

            addContentToFirstPage(document, firstPage, replacements);
            addContentToSecondPage(document, secondPage, dto);

            return saveDocumentToByteArray(document);
        }
    }

    protected void addContentToFirstPage(PDDocument document, PDPage page, Map<String, String> replacements) throws IOException {
        logger.info("Adding content to the first page.");

        addTextWithRectangle(document, page, "Заказ № 000-000",
                PAGE_INDENT + 5, PAGE_HEIGHT - PAGE_INDENT, 121);
        addTextWithRectangle(document, page, "Ворота Optima (заполн.: жалюзи), цельные",
                PAGE_INDENT + 126, PAGE_HEIGHT - PAGE_INDENT, 580);
        addTextWithRectangle(document, page, "Стр. 1/2",
                PAGE_INDENT + 706, PAGE_HEIGHT - PAGE_INDENT, 61);


        addImageToPdf(document, page, replacements, PAGE_INDENT, PAGE_HEIGHT - PAGE_INDENT - 5, PAGE_WIDTH - PAGE_INDENT * 2, 450);
        createPdfWithRectangle(document, page, PAGE_INDENT, PAGE_HEIGHT - PAGE_INDENT - 5, PAGE_WIDTH - PAGE_INDENT * 2, 450);

        logger.info("Adding signatures to the first page.");
        addTextWithRectangle(document, page, "Склад принял:", PAGE_INDENT + 400, LINE_SPACING * 4 - 8, 367);
        addTextWithRectangle(document, page, "ОТК, контроль качества:", PAGE_INDENT + 400, LINE_SPACING * 3 - 8, 367);
        addTextWithRectangle(document, page, "Подпись заказчика:", PAGE_INDENT + 400, LINE_SPACING * 2 - 8, 367);

        addTextWithRectangle(document, page, "Дата:", PAGE_INDENT + 20 * 3 + 5, LINE_SPACING * 2 - 8, 335);

        createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 400 - 5, 20 * 3);
        createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 20 * 3, 20 * 3);
    }

    protected void addContentToSecondPage(PDDocument document, PDPage page, Map<String, Object> dto) throws IOException {
        logger.info("Adding content to the second page.");

        addTextWithRectangle(document, page, "Заказ № 000-000",
                PAGE_INDENT + 5, PAGE_HEIGHT - PAGE_INDENT, 121);
        addTextWithRectangle(document, page, "Ворота Optima (заполн.: жалюзи), цельные",
                PAGE_INDENT + 126, PAGE_HEIGHT - PAGE_INDENT, 579);
        addTextWithRectangle(document, page, "Стр. 2/2",
                PAGE_INDENT + 705, PAGE_HEIGHT - PAGE_INDENT, 61);


        yPosition = PAGE_HEIGHT - PAGE_INDENT - LINE_SPACING;

        addRow(document, page, "Наименование", "Ед.", "Значение");
        addRow(document, page, "Количество столбов", "шт.",String.valueOf(dto.get("pillarCount")));
        addRow(document, page, "Балка направляющая","параметр", String.valueOf(dto.get("beam")));
        addRow(document, page, "Высота столба","параметр", String.valueOf(dto.get("pillarHeight")));
        addRow(document, page, "Столб на прямоугольном фланце","да/нет", String.valueOf(dto.get("pillarRectangularFlange")));
        addRow(document, page, "Вид столба","параметр", String.valueOf(dto.get("pillarType")));
        addRow(document, page, "Вид заполнения","параметр", String.valueOf(dto.get("typeFilling")));
        addRow(document, page, "Цвет изделия","параметр", dto.get("colorRal1") + " " + dto.get("colorRal2"));
        addRow(document, page, "Вид каркаса","мм.", String.valueOf(dto.get("pipe")));

        addWrappedTextToPdf(document, page, PAGE_INDENT, yPosition - 5, 700,
                "Комментарий к заказу: " + dto.get("comment"));


        addTextWithRectangle(document, page, "Склад принял:", PAGE_INDENT + 400, LINE_SPACING * 4 - 8, 367);
        addTextWithRectangle(document, page, "ОТК, контроль качества:", PAGE_INDENT + 400, LINE_SPACING * 3 - 8, 367);
        addTextWithRectangle(document, page, "Подпись заказчика:", PAGE_INDENT + 400, LINE_SPACING * 2 - 8, 367);

        addTextWithRectangle(document, page, "Дата:", PAGE_INDENT + 20 * 3 + 5, LINE_SPACING * 2 - 8, 335);

        createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 400 - 5, 20 * 3);
        createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 20 * 3, 20 * 3);
    }

    private void loadFont() throws IOException {
        try (InputStream fontStream = new ClassPathResource(FONT_PATH).getInputStream()) {
            FONT = PDType0Font.load(new PDDocument(), fontStream);
        }
    }

    private void addRow(PDDocument document, PDPage page,  String key, String type, String value) throws IOException {
        addTextWithRectangle(document, page, key, PAGE_INDENT + 5, yPosition, 300);
        addTextWithRectangle(document, page, type, PAGE_INDENT + 305, yPosition, 75);
        addTextWithRectangle(document, page, value, PAGE_INDENT + 380, yPosition, 386);

        yPosition -= LINE_SPACING;
    }

    public void addWrappedTextToPdf(PDDocument document, PDPage page, float startX, float startY, float maxWidth, String text) throws IOException {
        float lineHeight = 14;  // Интервал между строками

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine + (currentLine.length() == 0 ? "" : " ") + word;
            float textWidth = FONT.getStringWidth(testLine) / 1000 * FONT_SIZE;

            if (textWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        lines.add(currentLine.toString());  // Добавляем последнюю строку

        float currentY = startY;
        for (String line : lines) {
            addTextToPdf(document, page, line, startX, currentY);  // Переиспользуем существующий метод
            currentY -= lineHeight;
        }
    }

    private byte[] saveDocumentToByteArray(PDDocument document) throws IOException {
        logger.info("Saving PDF to byte array.");
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private PDDocument addTextToPdf(PDDocument document, PDPage page, String text, float x, float y) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.beginText();
            contentStream.setFont(FONT, FONT_SIZE);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        }
        return document;
    }

    private PDDocument addTextWithRectangle(PDDocument document, PDPage page, String text, float x, float y, float rectangleWidth) throws IOException {
        final float RECTANGLE_HEIGHT = 20;
        final float RECTANGLE_OFFSET = 5;
        final float TEXT_OFFSET_Y = 15;

        addTextToPdf(document, page, text, x, y);

        float rectX = x - RECTANGLE_OFFSET;
        float rectY = y + TEXT_OFFSET_Y;

        createPdfWithRectangle(document, page, rectX, rectY, rectangleWidth, RECTANGLE_HEIGHT);

        return document;
    }

    private PDDocument createPdfWithRectangle(PDDocument document, PDPage page, float x, float y, float width, float height) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.setStrokingColor(0, 0, 0);
            contentStream.setLineWidth(RECTANGLE_LINE_WIDTH);
            float correctedY = y - height;
            contentStream.addRect(x, correctedY, width, height);
            contentStream.stroke();
        }
        return document;
    }

    private PDDocument addImageToPdf(PDDocument document, PDPage page, Map<String, String> replacements, float x, float y, float width, float height) throws IOException {
        PngGenerator pngGenerator = new PngGenerator(inputFilePath);
        byte[] imageBytes = pngGenerator.getPNG(replacements);
        PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "png");

        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();

        float scale = Math.min(width / imageWidth, height / imageHeight);
        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;

        float correctedY = y - height;
        float offsetX = x + (width - scaledWidth) / 2;
        float offsetY = correctedY + (height - scaledHeight) / 2;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.drawImage(image, offsetX, offsetY, scaledWidth, scaledHeight);
        }
        return document;
    }
}
