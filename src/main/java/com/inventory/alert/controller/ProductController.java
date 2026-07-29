package com.inventory.alert.controller;

import com.inventory.alert.dto.request.ProductCreateRequest;
import com.inventory.alert.dto.request.ProductUpdateRequest;
import com.inventory.alert.dto.response.ErrorResponse;
import com.inventory.alert.dto.response.ProductResponse;
import com.inventory.alert.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create product", description = "SKU must be unique. Bean Validation applies to the body.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate SKU",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        log.info("API create product sku={}", request.getSku());
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + created.getId())).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List products",
            description = "Supports pagination and sorting (?page=&size=&sort=name,asc). "
                    + "Optional filter: active=true for active catalog only.")
    @ApiResponse(responseCode = "200", description = "Page of products")
    public ResponseEntity<Page<ProductResponse>> list(
            @Parameter(description = "When true, return only active products")
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("API list products active={} page={} size={}", active, pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductResponse> page = Boolean.TRUE.equals(active)
                ? productService.listActiveProducts(pageable)
                : productService.listProducts(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Case-insensitive partial match with pagination.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results"),
            @ApiResponse(responseCode = "400", description = "Missing/blank name", content = @Content)
    })
    public ResponseEntity<Page<ProductResponse>> search(
            @Parameter(description = "Name fragment", required = true, example = "widget")
            @RequestParam @NotBlank String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("API search products name={}", name);
        return ResponseEntity.ok(productService.searchProducts(name, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> getById(
            @Parameter(description = "Product id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> getBySku(
            @Parameter(description = "Business SKU", example = "SKU-100") @PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Partial update; null fields are ignored by the service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate SKU / optimistic lock", content = @Content)
    })
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        log.info("API update product id={}", id);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete product", description = "Sets active=false. History and alerts are retained.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Soft-deleted"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API soft-delete product id={}", id);
        productService.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
