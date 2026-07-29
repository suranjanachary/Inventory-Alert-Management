package com.inventory.alert.controller;

import com.inventory.alert.dto.request.StockMutationRequest;
import com.inventory.alert.dto.response.ErrorResponse;
import com.inventory.alert.dto.response.InventoryTransactionResponse;
import com.inventory.alert.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/purchase")
    @Operation(summary = "Purchase stock", description = "Increases available quantity and writes a PURCHASE ledger row.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Purchase recorded"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Optimistic lock conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryTransactionResponse> purchase(
            @Valid @RequestBody StockMutationRequest request) {
        log.info("API purchase productId={} qty={}", request.getProductId(), request.getQuantity());
        InventoryTransactionResponse body = inventoryService.purchaseStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/sale")
    @Operation(
            summary = "Sell stock",
            description = "Decreases stock, writes a SALE ledger row, and may create a PENDING LOW_STOCK alert.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sale recorded"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(
                    responseCode = "422",
                    description = "Insufficient stock or inactive product",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Optimistic lock conflict", content = @Content)
    })
    public ResponseEntity<InventoryTransactionResponse> sale(
            @Valid @RequestBody StockMutationRequest request) {
        log.info("API sale productId={} qty={}", request.getProductId(), request.getQuantity());
        InventoryTransactionResponse body = inventoryService.sellStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/history/{productId}")
    @Operation(summary = "Inventory history", description = "Paginated ledger for a product, newest first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History page"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Page<InventoryTransactionResponse>> history(
            @Parameter(description = "Product id", example = "1") @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API inventory history productId={}", productId);
        return ResponseEntity.ok(inventoryService.getInventoryHistory(productId, pageable));
    }
}
