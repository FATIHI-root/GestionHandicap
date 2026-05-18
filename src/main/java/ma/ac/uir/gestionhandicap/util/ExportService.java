package ma.ac.uir.gestionhandicap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportService {

    public static void export(Stage owner, String defaultName, String titre,
                              List<String> headers, List<List<String>> rows) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));

        File file = fc.showSaveDialog(owner);
        if (file == null) return;

        String name = file.getName().toLowerCase();
        try {
            if (name.endsWith(".pdf")) {
                exportPdf(file, titre, headers, rows);
            } else if (name.endsWith(".xlsx")) {
                exportExcel(file, titre, headers, rows);
            } else {
                if (!name.endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                exportCsv(file, headers, rows);
            }
            AlertUtil.showInfo("Export réussi", "Fichier enregistré :\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.showError("Export", "Erreur lors de l'export : " + e.getMessage());
        }
    }

    private static void exportCsv(File file, List<String> headers, List<List<String>> rows) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("﻿");
        appendCsvRow(sb, headers);
        for (List<String> row : rows) {
            appendCsvRow(sb, row);
        }
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void appendCsvRow(StringBuilder sb, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(';');
            String v = values.get(i) == null ? "" : values.get(i);
            v = v.replace("\"", "\"\"");
            if (v.contains(";") || v.contains("\"") || v.contains("\n")) {
                sb.append('"').append(v).append('"');
            } else {
                sb.append(v);
            }
        }
        sb.append("\r\n");
    }

    private static void exportExcel(File file, String titre,
                                    List<String> headers, List<List<String>> rows) throws IOException {
        Workbook wb = new XSSFWorkbook();
        try {
            Sheet sheet = wb.createSheet(titre == null ? "Export" : titre);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row excelRow = sheet.createRow(r + 1);
                List<String> data = rows.get(r);
                for (int c = 0; c < data.size(); c++) {
                    excelRow.createCell(c).setCellValue(data.get(c) == null ? "" : data.get(c));
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            FileOutputStream out = new FileOutputStream(file);
            try {
                wb.write(out);
            } finally {
                out.close();
            }
        } finally {
            wb.close();
        }
    }

    private static void exportPdf(File file, String titre,
                                  List<String> headers, List<List<String>> rows) throws IOException {
        PDDocument doc = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream content = new PDPageContentStream(doc, page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontReg = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 30;
            float yStart = page.getMediaBox().getHeight() - margin;
            float lineHeight = 14;
            float y = yStart;

            content.beginText();
            content.setFont(fontBold, 16);
            content.newLineAtOffset(margin, y);
            content.showText(titre == null ? "Export" : titre);
            content.endText();

            y -= 28;
            content.beginText();
            content.setFont(fontBold, 10);
            content.newLineAtOffset(margin, y);
            content.showText(joinRow(headers));
            content.endText();

            y -= lineHeight + 2;
            content.setLineWidth(0.5f);
            content.moveTo(margin, y);
            content.lineTo(page.getMediaBox().getWidth() - margin, y);
            content.stroke();
            y -= 8;

            int pageNum = 1;
            for (List<String> row : rows) {
                if (y < margin + lineHeight) {
                    content.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    doc.addPage(newPage);
                    content = new PDPageContentStream(doc, newPage);
                    pageNum++;
                    y = newPage.getMediaBox().getHeight() - margin;

                    content.beginText();
                    content.setFont(fontBold, 10);
                    content.newLineAtOffset(margin, y);
                    content.showText(joinRow(headers));
                    content.endText();
                    y -= lineHeight + 2;
                    content.setLineWidth(0.5f);
                    content.moveTo(margin, y);
                    content.lineTo(newPage.getMediaBox().getWidth() - margin, y);
                    content.stroke();
                    y -= 8;
                }

                content.beginText();
                content.setFont(fontReg, 9);
                content.newLineAtOffset(margin, y);
                content.showText(safe(joinRow(row)));
                content.endText();
                y -= lineHeight;
            }

            content.close();
            doc.save(file);
        } finally {
            doc.close();
        }
    }

    private static String joinRow(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append("   |   ");
            String v = values.get(i) == null ? "" : values.get(i);
            if (v.length() > 40) v = v.substring(0, 37) + "...";
            sb.append(v);
        }
        return sb.toString();
    }

    private static String safe(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 32 && c <= 126) {
                out.append(c);
            } else if (c == 'é' || c == 'è' || c == 'ê' || c == 'ë') {
                out.append('e');
            } else if (c == 'à' || c == 'â' || c == 'ä') {
                out.append('a');
            } else if (c == 'î' || c == 'ï') {
                out.append('i');
            } else if (c == 'ô' || c == 'ö') {
                out.append('o');
            } else if (c == 'ù' || c == 'û' || c == 'ü') {
                out.append('u');
            } else if (c == 'ç') {
                out.append('c');
            } else if (c == 'É' || c == 'È' || c == 'Ê') {
                out.append('E');
            } else if (c == '—' || c == '–') {
                out.append('-');
            } else if (c == '’' || c == '‘') {
                out.append('\'');
            } else {
                out.append('?');
            }
        }
        return out.toString();
    }
}
