package ru.chgzabor.pdf.service;

import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class DrawingCreator {
    private static final Logger logger = LoggerFactory.getLogger(DrawingCreator.class);
    private final String inputFilePath;

    private static final float FONT_SIZE_DRAWING = 34 * 2;
    private static final String FONT_PATH = "font/ArialCyr.TTF";
    private PDType0Font FONT;

    // Конструктор для инициализации пути к входному файлу
    public DrawingCreator(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public byte[] getPNG(Map<String, String> replacements) throws IOException {
        try (InputStream inputStream = new ClassPathResource(inputFilePath).getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            logger.info("PDF успешно загружен и масштабирован из класспа: {}", inputFilePath);

            // Загрузка шрифта
            try (InputStream fontStream = new ClassPathResource(FONT_PATH).getInputStream()) {
                FONT = PDType0Font.load(new PDDocument(), fontStream);
            }

            // Извлечение текста и позиций символов
            List<DrawingCreator.LetterWithPosition> letters = extractLettersWithPositions(document);
            logger.info("Текст извлечён из документа. Всего символов: {}", letters.size());

            // Замена символов в тексте
            processDocument(document, letters, replacements);

            // Рендерим изображение из первой страницы PDF
            BufferedImage image = renderPdfPageToImage(document, 0);
            logger.info("Изображение страницы успешно преобразовано в PNG");

            // Преобразование изображения в массив байтов
            return convertImageToByteArray(image);
        } catch (IOException e) {
            logger.error("Ошибка при обработке PDF: {}", e.getMessage());
            throw e;
        }
    }

    private BufferedImage renderPdfPageToImage(PDDocument document, int pageIndex) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        return renderer.renderImage(pageIndex);
    }

    private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", imageOutputStream);
        return imageOutputStream.toByteArray();
    }

    private List<DrawingCreator.LetterWithPosition> extractLettersWithPositions(PDDocument document) throws IOException {
        List<DrawingCreator.LetterWithPosition> letters = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            private int currentPageIndex = -1;

            @Override
            protected void startPage(PDPage page) throws IOException {
                currentPageIndex++;
                super.startPage(page);
            }

            @Override
            protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
                super.writeString(string, textPositions);
                for (TextPosition textPosition : textPositions) {
                    letters.add(new DrawingCreator.LetterWithPosition(
                            textPosition.getUnicode(),
                            textPosition.getXDirAdj(),
                            textPosition.getYDirAdj(),
                            currentPageIndex
                    ));
                }
            }
        };
        stripper.setStartPage(1);
        stripper.setEndPage(document.getNumberOfPages());
        stripper.getText(document);
        return letters;
    }

    private void replaceLetters(List<DrawingCreator.LetterWithPosition> letters, Map<String, String> replacements) {
        logger.info("Заменяем буквы в документе...");

        for (DrawingCreator.LetterWithPosition letter : letters) {
            String newCharacter = replacements.get(letter.getCharacter());
            if (newCharacter != null) {
                logger.info("Заменено '{}' на '{}' на странице {} (x={}, y={})",
                        letter.getCharacter(), newCharacter, letter.getPageIndex(), letter.getX(), letter.getY());
                letter.setCharacter(newCharacter);
            }
        }
    }

    public void processDocument(PDDocument document, List<DrawingCreator.LetterWithPosition> letters, Map<String, String> replacements) throws IOException {
        // Замена символов в тексте
        replaceLetters(letters, replacements);

        // Подстановка новых символов
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            PDPage page = document.getPage(i);
            float pageHeight = page.getMediaBox().getHeight();

            try (PDPageContentStream contentStream = createContentStream(document, page)) {
                // Для каждой страницы заменяем символы
                for (DrawingCreator.LetterWithPosition letter : letters) {
                    if (letter.getPageIndex() == i) {
                        float correctedY = pageHeight - letter.getY();
                        float textWidth = (FONT.getStringWidth(letter.getCharacter()) / 1000) * FONT_SIZE_DRAWING;
                        float textHeight = FONT_SIZE_DRAWING;

                        // Заполнение фона (удаление старого текста)
                        contentStream.setNonStrokingColor(Color.WHITE);
                        contentStream.addRect(letter.getX() - 1, correctedY - 2, textWidth + 2, textHeight + 4);
                        contentStream.fill();

                        // Отображение нового текста
                        contentStream.setNonStrokingColor(Color.BLACK);
                        contentStream.beginText();
                        contentStream.setFont(FONT, FONT_SIZE_DRAWING);
                        contentStream.setTextMatrix(1, 0, 0, 1, letter.getX(), correctedY);
                        contentStream.showText(letter.getCharacter());
                        contentStream.endText();
                    }
                }
            } catch (IOException e) {
                logger.error("Ошибка при обработке страницы {}", i, e);
                throw e;  // Пробрасываем исключение дальше для обработки на более высоком уровне
            }
        }
    }

    private PDPageContentStream createContentStream(PDDocument document, PDPage page) throws IOException {
        return new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
    }


    @Getter
    @Setter
    private static class LetterWithPosition {
        private String character;
        private float x;
        private float y;
        private int pageIndex;

        public LetterWithPosition(String character, float x, float y, int pageIndex) {
            this.character = character;
            this.x = x;
            this.y = y;
            this.pageIndex = pageIndex;
        }
    }
}
