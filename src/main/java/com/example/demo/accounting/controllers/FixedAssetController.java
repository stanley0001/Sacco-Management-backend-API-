package com.example.demo.accounting.controllers;

import com.example.demo.accounting.entities.AssetCategory;
import com.example.demo.accounting.entities.FixedAsset;
import com.example.demo.accounting.services.FixedAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting/fixed-assets")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Fixed Assets", description = "Fixed Asset Management & Depreciation")
public class FixedAssetController {

    private final FixedAssetService assetService;

    @PostMapping
    @Operation(summary = "Register new fixed asset")
    public ResponseEntity<?> registerAsset(
            @RequestBody FixedAsset asset,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            FixedAsset created = assetService.registerAsset(asset, createdBy);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error registering asset", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fixed asset")
    public ResponseEntity<?> updateAsset(
            @PathVariable Long id,
            @RequestBody FixedAsset asset) {
        try {
            FixedAsset updated = assetService.updateAsset(id, asset);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating asset", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all assets")
    public ResponseEntity<List<FixedAsset>> getAllAssets() {
        List<FixedAsset> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active assets")
    public ResponseEntity<List<FixedAsset>> getActiveAssets() {
        List<FixedAsset> assets = assetService.getActiveAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/total-value")
    @Operation(summary = "Get total asset value")
    public ResponseEntity<Map<String, Double>> getTotalAssetValue() {
        Double total = assetService.getTotalAssetValue();
        return ResponseEntity.ok(Map.of("totalValue", total));
    }

    @PostMapping("/depreciation/calculate")
    @Operation(summary = "Calculate monthly depreciation")
    public ResponseEntity<?> calculateDepreciation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        try {
            assetService.calculateMonthlyDepreciation(month);
            return ResponseEntity.ok(Map.of(
                    "message", "Depreciation calculated successfully for " + month,
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Error calculating depreciation", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/dispose")
    @Operation(summary = "Dispose asset")
    public ResponseEntity<?> disposeAsset(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String disposedBy = authentication != null ? authentication.getName() : "system";
            
            Double disposalValue = request.get("disposalValue") != null ? 
                    Double.parseDouble(request.get("disposalValue").toString()) : 0.0;
            
            LocalDate disposalDate = request.get("disposalDate") != null ?
                    LocalDate.parse(request.get("disposalDate").toString()) : LocalDate.now();
            
            FixedAsset disposed = assetService.disposeAsset(id, disposalValue, disposalDate, disposedBy);
            return ResponseEntity.ok(disposed);
        } catch (Exception e) {
            log.error("Error disposing asset", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Asset Categories ==========

    @PostMapping("/categories")
    @Operation(summary = "Create asset category")
    public ResponseEntity<?> createCategory(@RequestBody AssetCategory category) {
        try {
            AssetCategory created = assetService.createCategory(category);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating category", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all asset categories")
    public ResponseEntity<List<AssetCategory>> getAllCategories() {
        List<AssetCategory> categories = assetService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories/initialize")
    @Operation(summary = "Initialize standard asset categories")
    public ResponseEntity<Map<String, String>> initializeCategories() {
        try {
            assetService.initializeStandardCategories();
            return ResponseEntity.ok(Map.of(
                    "message", "Standard asset categories initialized successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Error initializing categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage(), "status", "error"));
        }
    }
}
