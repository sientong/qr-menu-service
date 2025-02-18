package com.qrmenu.controller;

import com.qrmenu.dto.stock.BatchStockUpdateRequest;
import com.qrmenu.dto.stock.StockAdjustmentRequest;
import com.qrmenu.dto.stock.StockResponse;
import com.qrmenu.dto.stock.StockHistoryResponse;
import com.qrmenu.dto.stock.StockReportRequest;
import com.qrmenu.dto.stock.StockReportSummary;
import com.qrmenu.dto.stock.StockValuationResponse;
import com.qrmenu.dto.stock.StockAlertResponse;
import com.qrmenu.dto.stock.BatchValuationRequest;
import com.qrmenu.service.StockManagementService;
import com.qrmenu.service.StockAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock Management", description = "Stock management APIs")
public class StockManagementController {

    private final StockManagementService stockManagementService;
    private final StockAlertService stockAlertService;

    @Operation(
        summary = "Adjust stock quantity",
        description = "Adjust the stock quantity of a menu item"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Stock adjusted successfully",
            content = @Content(schema = @Schema(implementation = StockResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu item not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or insufficient stock"
        )
    })
    @PostMapping("/{menuItemId}/adjust")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<StockResponse> adjustStock(
            @Parameter(description = "Menu item ID", required = true)
            @PathVariable Long menuItemId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockManagementService.adjustStock(menuItemId, request));
    }

    @Operation(
        summary = "Batch update stock",
        description = "Update stock quantities for multiple menu items"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Stock updated successfully",
            content = @Content(schema = @Schema(implementation = StockResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "One or more menu items not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    @PostMapping("/batch-update")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<StockResponse>> batchUpdateStock(
            @Valid @RequestBody BatchStockUpdateRequest request) {
        return ResponseEntity.ok(stockManagementService.batchUpdateStock(request));
    }

    @GetMapping("/{menuItemId}/history")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<StockHistoryResponse>> getStockHistory(
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(stockManagementService.getStockHistory(menuItemId));
    }

    @PostMapping("/report")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<StockHistoryResponse>> generateReport(
            @Valid @RequestBody StockReportRequest request) {
        return ResponseEntity.ok(stockManagementService.generateStockReport(request));
    }

    @PostMapping("/report/summary")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<StockReportSummary> generateReportSummary(
            @Valid @RequestBody StockReportRequest request) {
        return ResponseEntity.ok(stockManagementService.generateReportSummary(request));
    }

    @PostMapping("/report/export")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<byte[]> exportReport(@Valid @RequestBody StockReportRequest request) {
        byte[] report = stockManagementService.exportStockReport(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
    }

    @PostMapping("/valuation/batch-update")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<StockValuationResponse>> batchUpdateValuation(
            @Valid @RequestBody BatchValuationRequest request) {
        return ResponseEntity.ok(stockManagementService.batchUpdateValuation(request));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<StockAlertResponse>> getActiveAlerts() {
        return ResponseEntity.ok(stockAlertService.getActiveAlerts());
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable Long alertId) {
        stockAlertService.acknowledgeAlert(alertId);
        return ResponseEntity.ok().build();
    }
} 