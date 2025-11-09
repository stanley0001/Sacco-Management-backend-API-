package com.example.demo.finance.assets.controllers;

import com.example.demo.finance.assets.entities.Asset;
import com.example.demo.finance.assets.services.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Assets", description = "Asset Management for Asset-Based Financing")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @Operation(summary = "Register new asset")
    public ResponseEntity<Asset> createAsset(
            @RequestBody Asset asset,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            asset.setCreatedBy(createdBy);
            
            // Generate asset number if not provided
            if (asset.getAssetNumber() == null || asset.getAssetNumber().isEmpty()) {
                String assetNumber = assetService.generateAssetNumber(asset.getAssetCategory());
                asset.setAssetNumber(assetNumber);
            }
            
            Asset created = assetService.createAsset(asset);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating asset", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable Long id,
            @RequestBody Asset asset) {
        try {
            Asset updated = assetService.updateAsset(id, asset);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating asset", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/valuation")
    @Operation(summary = "Update asset valuation")
    public ResponseEntity<Asset> updateValuation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Double marketValue = Double.parseDouble(request.get("marketValue").toString());
            LocalDate valuationDate = LocalDate.parse(request.get("valuationDate").toString());
            String valuedBy = authentication != null ? authentication.getName() : "system";
            
            Asset updated = assetService.updateValuation(id, marketValue, valuationDate, valuedBy);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating valuation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/pledge")
    @Operation(summary = "Pledge asset as collateral for loan")
    public ResponseEntity<Asset> pledgeAsCollateral(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Long loanAccountId = Long.parseLong(request.get("loanAccountId").toString());
            String loanAccountNumber = request.get("loanAccountNumber").toString();
            Double loanAmount = Double.parseDouble(request.get("loanAmount").toString());
            
            Asset pledged = assetService.pledgeAsCollateral(id, loanAccountId, loanAccountNumber, loanAmount);
            return ResponseEntity.ok(pledged);
        } catch (Exception e) {
            log.error("Error pledging asset", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release asset from collateral")
    public ResponseEntity<Asset> releaseCollateral(@PathVariable Long id) {
        try {
            Asset released = assetService.releaseCollateral(id);
            return ResponseEntity.ok(released);
        } catch (Exception e) {
            log.error("Error releasing asset", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/depreciation")
    @Operation(summary = "Calculate and record depreciation")
    public ResponseEntity<Asset> calculateDepreciation(@PathVariable Long id) {
        try {
            Asset asset = assetService.calculateDepreciation(id);
            return ResponseEntity.ok(asset);
        } catch (Exception e) {
            log.error("Error calculating depreciation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/inspection")
    @Operation(summary = "Record asset inspection")
    public ResponseEntity<Asset> recordInspection(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String inspectedBy = authentication != null ? authentication.getName() : "system";
            String condition = request.get("condition");
            String notes = request.get("notes");
            
            Asset inspected = assetService.recordInspection(id, inspectedBy, condition, notes);
            return ResponseEntity.ok(inspected);
        } catch (Exception e) {
            log.error("Error recording inspection", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/dispose")
    @Operation(summary = "Dispose asset")
    public ResponseEntity<Asset> disposeAsset(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String reason = request.get("reason").toString();
            Double disposalValue = request.containsKey("disposalValue") ? 
                    Double.parseDouble(request.get("disposalValue").toString()) : 0.0;
            
            Asset disposed = assetService.disposeAsset(id, reason, disposalValue);
            return ResponseEntity.ok(disposed);
        } catch (Exception e) {
            log.error("Error disposing asset", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    @Operation(summary = "Get all assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        List<Asset> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{assetNumber}")
    @Operation(summary = "Get asset by number")
    public ResponseEntity<Asset> getAssetByNumber(@PathVariable String assetNumber) {
        return assetService.getAssetByNumber(assetNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get assets by owner")
    public ResponseEntity<List<Asset>> getAssetsByOwner(@PathVariable Long ownerId) {
        List<Asset> assets = assetService.getAssetsByOwner(ownerId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/owner/{ownerId}/value")
    @Operation(summary = "Get total asset value by owner")
    public ResponseEntity<Map<String, Object>> getTotalAssetValueByOwner(@PathVariable Long ownerId) {
        Double totalValue = assetService.getTotalAssetValueByOwner(ownerId);
        return ResponseEntity.ok(Map.of(
                "ownerId", ownerId,
                "totalAssetValue", totalValue,
                "currency", "KES"
        ));
    }

    @GetMapping("/available-collateral")
    @Operation(summary = "Get assets available for collateral")
    public ResponseEntity<List<Asset>> getAvailableForCollateral() {
        List<Asset> assets = assetService.getAvailableForCollateral();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/loan/{loanAccountId}")
    @Operation(summary = "Get assets for loan")
    public ResponseEntity<List<Asset>> getAssetsForLoan(@PathVariable Long loanAccountId) {
        List<Asset> assets = assetService.getAssetsForLoan(loanAccountId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/search")
    @Operation(summary = "Search assets")
    public ResponseEntity<List<Asset>> searchAssets(@RequestParam String searchTerm) {
        List<Asset> assets = assetService.searchAssets(searchTerm);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/insurance-expiring")
    @Operation(summary = "Get assets with insurance expiring within specified days")
    public ResponseEntity<List<Asset>> getInsuranceExpiring(
            @RequestParam(defaultValue = "30") int days) {
        List<Asset> assets = assetService.getInsuranceExpiringWithin(days);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/inspections-due")
    @Operation(summary = "Get assets with inspections due")
    public ResponseEntity<List<Asset>> getInspectionsDue() {
        List<Asset> assets = assetService.getInspectionsDue();
        return ResponseEntity.ok(assets);
    }
}
