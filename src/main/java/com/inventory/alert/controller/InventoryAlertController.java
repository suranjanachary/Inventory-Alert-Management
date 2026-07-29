package com.inventory.alert.controller;

import com.inventory.alert.dto.response.ErrorResponse;
import com.inventory.alert.dto.response.InventoryAlertResponse;
import com.inventory.alert.service.InventoryAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts")
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    @GetMapping
    @Operation(summary = "List all alerts", description = "Paginated alert history across products.")
    @ApiResponse(responseCode = "200", description = "Page of alerts")
    public ResponseEntity<Page<InventoryAlertResponse>> listAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API list alerts page={}", pageable.getPageNumber());
        return ResponseEntity.ok(inventoryAlertService.getAlerts(pageable));
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending alerts", description = "Operational inbox for unresolved LOW_STOCK alerts.")
    @ApiResponse(responseCode = "200", description = "Page of pending alerts")
    public ResponseEntity<Page<InventoryAlertResponse>> listPending(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("API list pending alerts");
        return ResponseEntity.ok(inventoryAlertService.getPendingAlerts(pageable));
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolve alert", description = "Marks alert RESOLVED. Already-resolved alerts return 409.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resolved"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Already resolved",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryAlertResponse> resolve(
            @Parameter(description = "Alert id", example = "1") @PathVariable Long id) {
        log.info("API resolve alert id={}", id);
        return ResponseEntity.ok(inventoryAlertService.resolveAlert(id));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Alerts for a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of alerts"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Page<InventoryAlertResponse>> byProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API alerts by productId={}", productId);
        return ResponseEntity.ok(inventoryAlertService.getAlertsByProduct(productId, pageable));
    }
}
