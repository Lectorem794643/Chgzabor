package ru.chgzabor.pdf.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class PageContentGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PageContentGenerator.class);

    private static PDType0Font FONT;
    private static final String FONT_PATH="font/ArialCyr.TTF";

    private static final float RECTANGLE_LINE_WIDTH = 1;
    private static final float PAGE_WIDTH = 842;
    private static final float PAGE_HEIGHT = 595;
    private static final float PAGE_INDENT = 40;
    private static final float LINE_SPACING = 20;
    private static final float FONT_SIZE = 14;

    private final PdfDocumentHelper pdfHelper;

    public PageContentGenerator() {
        try {
            FONT = loadFont(); // Загружаем шрифт при каждом запросе
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pdfHelper = new PdfDocumentHelper(FONT, PAGE_INDENT, LINE_SPACING, FONT_SIZE, RECTANGLE_LINE_WIDTH);
    }

    public PDType0Font loadFont() throws IOException {
        try (InputStream fontStream = new ClassPathResource(FONT_PATH).getInputStream()) {
            return PDType0Font.load(new PDDocument(), fontStream);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить шрифт", e);
        }
    }

    public byte[] run(String drawingId, Map<String, String> replacements, Map<String, Object> dto) throws IOException {
        try (PDDocument document = new PDDocument()) {
            logger.info("Starting PDF generation.");

            PDPage firstPage = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            PDPage secondPage = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            document.addPage(firstPage);
            document.addPage(secondPage);

            addContentToFirstPage(document, firstPage, drawingId, replacements, dto);
            addContentToSecondPage(document, secondPage, dto);

            return pdfHelper.saveDocumentToByteArray(document);
        } catch (IOException e) {
            logger.error("Error generating PDF", e);
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    protected void addContentToFirstPage(PDDocument document, PDPage page,String drawingId, Map<String, String> replacements, Map<String, Object> dto) throws IOException {
        logger.info("Adding content to the first page.");

        pdfHelper.addTextWithRectangle(document, page, String.valueOf(dto.get("orderNumber")),
                PAGE_INDENT + 5, PAGE_HEIGHT - PAGE_INDENT, 121);
        pdfHelper.addTextWithRectangle(document, page, String.valueOf(dto.get("drawingName")),
                PAGE_INDENT + 126, PAGE_HEIGHT - PAGE_INDENT, 579);
        pdfHelper.addTextWithRectangle(document, page, "Стр. 2/2",
                PAGE_INDENT + 705, PAGE_HEIGHT - PAGE_INDENT, 61);


        pdfHelper.addImageToPdf(document, page, drawingId, replacements, PAGE_INDENT, PAGE_HEIGHT - PAGE_INDENT - 5, PAGE_WIDTH - PAGE_INDENT * 2, 450);
        pdfHelper.createPdfWithRectangle(document, page, PAGE_INDENT, PAGE_HEIGHT - PAGE_INDENT - 5, PAGE_WIDTH - PAGE_INDENT * 2, 450);

        logger.info("Adding signatures to the first page.");
        pdfHelper.addTextWithRectangle(document, page, "Склад принял:", PAGE_INDENT + 400, LINE_SPACING * 4 - 8, 367);
        pdfHelper.addTextWithRectangle(document, page, "ОТК, контроль качества:", PAGE_INDENT + 400, LINE_SPACING * 3 - 8, 367);
        pdfHelper.addTextWithRectangle(document, page, "Подпись заказчика:", PAGE_INDENT + 400, LINE_SPACING * 2 - 8, 367);

        pdfHelper.addTextWithRectangle(document, page, "Дата:", PAGE_INDENT + 20 * 3 + 5, LINE_SPACING * 2 - 8, 335);

        pdfHelper.createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 400 - 5, 20 * 3);
        pdfHelper.createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 20 * 3, 20 * 3);
    }

    protected void addContentToSecondPage(PDDocument document, PDPage page, Map<String, Object> dto) throws IOException {
        logger.info("Adding content to the second page.");

        pdfHelper.addTextWithRectangle(document, page, String.valueOf(dto.get("orderNumber")),
                PAGE_INDENT + 5, PAGE_HEIGHT - PAGE_INDENT, 121);
        pdfHelper.addTextWithRectangle(document, page, String.valueOf(dto.get("drawingName")),
                PAGE_INDENT + 126, PAGE_HEIGHT - PAGE_INDENT, 579);
        pdfHelper.addTextWithRectangle(document, page, "Стр. 2/2",
                PAGE_INDENT + 705, PAGE_HEIGHT - PAGE_INDENT, 61);


        pdfHelper.yPosition = PAGE_HEIGHT - PAGE_INDENT - LINE_SPACING;

        pdfHelper.addRow(document, page, "Наименование", "Ед.", "Значение");
        pdfHelper.addRow(document, page, "Количество столбов", "шт.",String.valueOf(dto.get("pillarCount")));
        pdfHelper.addRow(document, page, "Балка направляющая","параметр", String.valueOf(dto.get("beam")));
        pdfHelper.addRow(document, page, "Высота столба","параметр", String.valueOf(dto.get("pillarHeight")));
        pdfHelper.addRow(document, page, "Столб на прямоугольном фланце","да/нет", String.valueOf(dto.get("pillarRectangularFlange")));
        pdfHelper.addRow(document, page, "Вид столба","параметр", String.valueOf(dto.get("pillarType")));
        pdfHelper.addRow(document, page, "Вид заполнения","параметр", String.valueOf(dto.get("typeFilling")));
        pdfHelper.addRow(document, page, "Цвет изделия","параметр", dto.get("colorRal1") + " " + dto.get("colorRal2"));
        pdfHelper.addRow(document, page, "Вид каркаса","мм.", String.valueOf(dto.get("pipe")));

        pdfHelper.addWrappedTextToPdf(document, page, PAGE_INDENT, 700,
                "Комментарий к заказу: " + dto.get("comment"));


        pdfHelper.addTextWithRectangle(document, page, "Склад принял:", PAGE_INDENT + 400, LINE_SPACING * 4 - 8, 367);
        pdfHelper.addTextWithRectangle(document, page, "ОТК, контроль качества:", PAGE_INDENT + 400, LINE_SPACING * 3 - 8, 367);
        pdfHelper.addTextWithRectangle(document, page, "Подпись заказчика:", PAGE_INDENT + 400, LINE_SPACING * 2 - 8, 367);

        pdfHelper.addTextWithRectangle(document, page, "Дата:", PAGE_INDENT + 20 * 3 + 5, LINE_SPACING * 2 - 8, 335);

        pdfHelper.createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 400 - 5, 20 * 3);
        pdfHelper.createPdfWithRectangle(document, page, PAGE_INDENT, LINE_SPACING * 4 + 7, 20 * 3, 20 * 3);
    }
}
