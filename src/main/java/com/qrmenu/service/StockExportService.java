package com.qrmenu.service;

import com.qrmenu.dto.stock.StockHistoryResponse;
import com.qrmenu.dto.stock.StockReportSummary;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportToExcel(List<StockHistoryResponse> history, StockReportSummary summary) {
        try (Workbook workbook = new XSSFWorkbook()) {
            createSummarySheet(workbook, summary);
            createHistorySheet(workbook, history);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export stock report", e);
        }
    }

    private void createSummarySheet(Workbook workbook, StockReportSummary summary) {
        Sheet sheet = workbook.createSheet("Summary");
        int rowNum = 0;

        createSummaryRow(sheet, rowNum++, "Total Items", String.valueOf(summary.getTotalItems()));
        createSummaryRow(sheet, rowNum++, "Total Stock Value", summary.getTotalStockValue().toString());
        createSummaryRow(sheet, rowNum++, "Low Stock Items", String.valueOf(summary.getLowStockItemsCount()));
        createSummaryRow(sheet, rowNum++, "Out of Stock Items", String.valueOf(summary.getOutOfStockItemsCount()));
        createSummaryRow(sheet, rowNum++, "Average Stock Level", String.format("%.2f", summary.getAverageStockLevel()));
        createSummaryRow(sheet, rowNum++, "Most Adjusted Item", summary.getMostAdjustedItem());
        createSummaryRow(sheet, rowNum, "Most Valuable Item", summary.getMostValuableItem());
    }

    private void createHistorySheet(Workbook workbook, List<StockHistoryResponse> history) {
        Sheet sheet = workbook.createSheet("History");
        Row headerRow = sheet.createRow(0);
        
        // Create headers
        String[] headers = {"Date", "Item", "Previous Qty", "New Qty", "Adjustment", "Type", "Adjusted By", "Notes"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data rows
        int rowNum = 1;
        for (StockHistoryResponse entry : history) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getAdjustedAt().format(DATE_FORMATTER));
            row.createCell(1).setCellValue(entry.getMenuItemName());
            row.createCell(2).setCellValue(entry.getPreviousQuantity());
            row.createCell(3).setCellValue(entry.getNewQuantity());
            row.createCell(4).setCellValue(entry.getAdjustmentQuantity());
            row.createCell(5).setCellValue(entry.getAdjustmentType().toString());
            row.createCell(6).setCellValue(entry.getAdjustedBy());
            row.createCell(7).setCellValue(entry.getNotes());
        }

        // Autosize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummaryRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
} 