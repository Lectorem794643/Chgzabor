package ru.chgzabor.pdf.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PdfDocumentHelper {
    private static final Logger logger = LoggerFactory.getLogger(PdfDocumentHelper.class);

    private static PDType0Font FONT;

    private static float PAGE_INDENT = 40;
    private static float LINE_SPACING = 20;
    private static float FONT_SIZE = 14;
    private static float RECTANGLE_LINE_WIDTH = 1;

    float yPosition; // Костыль чутка

    PdfDocumentHelper(PDType0Font FONT, float PAGE_INDENT, float LINE_SPACING, float FONT_SIZE, float RECTANGLE_LINE_WIDTH) {
        PdfDocumentHelper.FONT = FONT;
        PdfDocumentHelper.PAGE_INDENT = PAGE_INDENT;
        PdfDocumentHelper.LINE_SPACING = LINE_SPACING;
        PdfDocumentHelper.FONT_SIZE = FONT_SIZE;
        PdfDocumentHelper.RECTANGLE_LINE_WIDTH = RECTANGLE_LINE_WIDTH;
    }


    public void addRow(PDDocument document, PDPage page,  String key, String type, String value) throws IOException {
        addTextWithRectangle(document, page, key, PAGE_INDENT + 5, yPosition, 300);
        addTextWithRectangle(document, page, type, PAGE_INDENT + 305, yPosition, 75);
        addTextWithRectangle(document, page, value, PAGE_INDENT + 380, yPosition, 386);

        yPosition -= LINE_SPACING;
    }

    public void addWrappedTextToPdf(PDDocument document, PDPage page, float startX, float maxWidth, String text) throws IOException {
        float lineHeight = 14;  // Интервал между строками
        float startY = yPosition - 5;

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

    public byte[] saveDocumentToByteArray(PDDocument document) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            if (document == null) {
                throw new IOException("Документ не может быть null");
            }
            logger.info("Сохраняем документ в байтовый массив...");

            try {
                document.save(byteArrayOutputStream);  // Сохраняем документ в байтовый массив
            } catch (IOException e) {
                logger.error("Ошибка при сохранении документа, возможно проблема с шрифтами", e);
                throw new IOException("Ошибка при сохранении документа в байтовый массив", e);
            } finally {
                document.close();  // Закрываем документ вручную
            }

            logger.info("Документ успешно сохранен в байтовый массив.");
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Ошибка при сохранении документа в байтовый массив", e);
            throw new IOException("Ошибка при сохранении документа в байтовый массив", e);
        }
    }

    public PDDocument addTextToPdf(PDDocument document, PDPage page, String text, float x, float y) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.beginText();
            contentStream.setFont(FONT, FONT_SIZE);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        }
        return document;
    }

    public PDDocument addTextWithRectangle(PDDocument document, PDPage page, String text, float x, float y, float rectangleWidth) throws IOException {
        final float RECTANGLE_HEIGHT = 20;
        final float RECTANGLE_OFFSET = 5;
        final float TEXT_OFFSET_Y = 15;

        addTextToPdf(document, page, text, x, y);

        float rectX = x - RECTANGLE_OFFSET;
        float rectY = y + TEXT_OFFSET_Y;

        createPdfWithRectangle(document, page, rectX, rectY, rectangleWidth, RECTANGLE_HEIGHT);

        return document;
    }

    public PDDocument createPdfWithRectangle(PDDocument document, PDPage page, float x, float y, float width, float height) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.setStrokingColor(0, 0, 0);
            contentStream.setLineWidth(RECTANGLE_LINE_WIDTH);
            float correctedY = y - height;
            contentStream.addRect(x, correctedY, width, height);
            contentStream.stroke();
        }
        return document;
    }

    public PDDocument addImageToPdf(PDDocument document, PDPage page, String drawingId, Map<String, String> replacements,
                                    float x, float y, float width, float height) throws IOException {
        // Логирование начала выполнения метода
        logger.info("Начало выполнения addImageToPdf с параметрами: x={}, y={}, width={}, height={}", x, y, width, height);

        // Генерация изображения PNG
        logger.info("Генерация PNG изображения через DrawingCreator");
        DrawingCreator drawingCreator = new DrawingCreator(drawingId);
        byte[] pngBytes = drawingCreator.getPNG(replacements);

        logger.info("PNG изображение успешно сгенерировано, размер в байтах: {}", pngBytes.length);

        // Создание PDImageXObject из массива байтов PNG
        PDImageXObject image = PDImageXObject.createFromByteArray(document, pngBytes, drawingId);
        logger.info("PDImageXObject успешно создан");

        // Добавление изображения на страницу
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            logger.info("Добавление изображения на страницу");

            // Расчёт корректного масштабирования для изображения
            float scaleX = width / image.getWidth();
            float scaleY = height / image.getHeight();
            float scale = Math.min(scaleX, scaleY);

            // Вычисляем итоговые размеры изображения с учётом масштабирования
            float scaledWidth = image.getWidth() * scale;
            float scaledHeight = image.getHeight() * scale;

            // Корректируем позицию Y для правильного отображения
            float correctedY = y - height;

            // Вычисляем смещения для центрирования изображения внутри заданной области
            float offsetX = x + (width - scaledWidth) / 2;
            float offsetY = correctedY + (height - scaledHeight) / 2;

            logger.info("Масштабирование рассчитано: scale={}, scaledWidth={}, scaledHeight={}", scale, scaledWidth, scaledHeight);
            logger.info("Смещения рассчитаны: offsetX={}, offsetY={}", offsetX, offsetY);

            // Рисуем изображение на странице
            contentStream.drawImage(image, offsetX, offsetY, scaledWidth, scaledHeight);
            logger.info("Изображение успешно добавлено на страницу");
        } catch (IOException e) {
            logger.error("Ошибка при работе с PDPageContentStream: {}", e.getMessage(), e);
            throw e;
        }

        // Завершаем выполнение метода
        logger.info("Завершение выполнения addImageToPdf");
        return document;
    }
}
