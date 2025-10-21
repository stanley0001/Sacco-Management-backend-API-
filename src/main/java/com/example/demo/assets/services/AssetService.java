package com.example.demo.assets.services;

import com.example.demo.assets.entities.Asset;
import com.example.demo.assets.repositories.AssetRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AssetService {

    private final AssetRepo assetRepo;

    @Transactional
    public Asset createAsset(Asset asset) {
        if (assetRepo.existsByAssetNumber(asset.getAssetNumber())) {
            throw new RuntimeException("Asset number already exists: " + asset.getAssetNumber());
        }

        // Initialize values
        asset.setBookValue(asset.getPurchasePrice());
        asset.setCurrentValue(asset.getPurchasePrice());
        asset.setAccumulatedDepreciation(0.0);

        return assetRepo.save(asset);
    }

    @Transactional
    public Asset updateAsset(Long id, Asset asset) {
        Asset existing = assetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));

        existing.setAssetName(asset.getAssetName());
        existing.setDescription(asset.getDescription());
        existing.setCurrentValue(asset.getCurrentValue());
        existing.setMarketValue(asset.getMarketValue());
        existing.setLocation(asset.getLocation());
        existing.setAddress(asset.getAddress());
        existing.setCondition(asset.getCondition());
        existing.setNotes(asset.getNotes());

        return assetRepo.save(existing);
    }

    @Transactional
    public Asset updateValuation(Long id, Double marketValue, LocalDate valuationDate, String valuedBy) {
        Asset asset = assetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));

        asset.setMarketValue(marketValue);
        asset.setCurrentValue(marketValue);
        asset.setValuationDate(valuationDate);
        asset.setValuedBy(valuedBy);

        log.info("Asset {} revalued to {}", asset.getAssetNumber(), marketValue);
        return assetRepo.save(asset);
    }

    @Transactional
    public Asset pledgeAsCollateral(Long assetId, Long loanAccountId, String loanAccountNumber, Double loanAmount) {
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        if (asset.getCollateralStatus() != Asset.CollateralStatus.AVAILABLE) {
            throw new RuntimeException("Asset is not available for collateral");
        }

        if (asset.getStatus() != Asset.AssetStatus.ACTIVE) {
            throw new RuntimeException("Only active assets can be pledged as collateral");
        }

        // Calculate Loan-to-Value ratio
        double ltv = (loanAmount / asset.getCurrentValue()) * 100;
        
        asset.setCollateralStatus(Asset.CollateralStatus.PLEDGED);
        asset.setLinkedLoanAccountId(loanAccountId);
        asset.setLoanAccountNumber(loanAccountNumber);
        asset.setLoanToValueRatio(ltv);

        log.info("Asset {} pledged as collateral for loan {} (LTV: {}%)", 
                 asset.getAssetNumber(), loanAccountNumber, ltv);
        
        return assetRepo.save(asset);
    }

    @Transactional
    public Asset releaseCollateral(Long assetId) {
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        if (asset.getCollateralStatus() != Asset.CollateralStatus.PLEDGED) {
            throw new RuntimeException("Asset is not currently pledged");
        }

        asset.setCollateralStatus(Asset.CollateralStatus.RELEASED);
        asset.setLinkedLoanAccountId(null);
        asset.setLoanAccountNumber(null);
        asset.setLoanToValueRatio(null);

        // After some time, make it available again
        // This can be done through a separate process or immediately
        asset.setCollateralStatus(Asset.CollateralStatus.AVAILABLE);

        log.info("Asset {} released from collateral", asset.getAssetNumber());
        return assetRepo.save(asset);
    }

    @Transactional
    public Asset calculateDepreciation(Long assetId) {
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        if (asset.getDepreciationMethod() == Asset.DepreciationMethod.NONE || 
            asset.getDepreciationMethod() == null) {
            log.info("Asset {} has no depreciation method", asset.getAssetNumber());
            return asset;
        }

        if (asset.getPurchaseDate() == null) {
            throw new RuntimeException("Purchase date is required for depreciation calculation");
        }

        // Calculate years since purchase
        Period period = Period.between(asset.getPurchaseDate(), LocalDate.now());
        int years = period.getYears();
        int months = period.getMonths();
        double fractionalYears = years + (months / 12.0);

        double newDepreciation = 0.0;

        switch (asset.getDepreciationMethod()) {
            case STRAIGHT_LINE:
                if (asset.getUsefulLifeYears() != null && asset.getUsefulLifeYears() > 0) {
                    double salvageValue = asset.getSalvageValue() != null ? asset.getSalvageValue() : 0.0;
                    double depreciableAmount = asset.getPurchasePrice() - salvageValue;
                    double annualDepreciation = depreciableAmount / asset.getUsefulLifeYears();
                    newDepreciation = Math.min(annualDepreciation * fractionalYears, depreciableAmount);
                }
                break;

            case DECLINING_BALANCE:
                if (asset.getDepreciationRate() != null) {
                    double rate = asset.getDepreciationRate() / 100;
                    double remainingValue = asset.getPurchasePrice();
                    for (int i = 0; i < fractionalYears; i++) {
                        remainingValue *= (1 - rate);
                    }
                    newDepreciation = asset.getPurchasePrice() - remainingValue;
                }
                break;

            case DOUBLE_DECLINING_BALANCE:
                if (asset.getUsefulLifeYears() != null && asset.getUsefulLifeYears() > 0) {
                    double rate = 2.0 / asset.getUsefulLifeYears();
                    double remainingValue = asset.getPurchasePrice();
                    for (int i = 0; i < fractionalYears; i++) {
                        remainingValue *= (1 - rate);
                    }
                    newDepreciation = asset.getPurchasePrice() - remainingValue;
                }
                break;

            default:
                log.warn("Depreciation method {} not fully implemented", asset.getDepreciationMethod());
        }

        asset.setAccumulatedDepreciation(newDepreciation);
        asset.calculateBookValue();

        log.info("Depreciation calculated for asset {}: Accumulated = {}, Book Value = {}", 
                 asset.getAssetNumber(), newDepreciation, asset.getBookValue());

        return assetRepo.save(asset);
    }

    @Transactional
    public Asset recordInspection(Long assetId, String inspectedBy, String condition, String notes) {
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        asset.setLastInspectedBy(inspectedBy);
        asset.setLastInspectionDate(LocalDate.now());
        asset.setCondition(condition);
        
        // Schedule next inspection (e.g., 6 months later)
        asset.setNextInspectionDate(LocalDate.now().plusMonths(6));
        
        String existingNotes = asset.getNotes() != null ? asset.getNotes() : "";
        asset.setNotes(existingNotes + "\n[" + LocalDate.now() + "] Inspection by " + 
                      inspectedBy + ": " + notes);

        log.info("Inspection recorded for asset {}", asset.getAssetNumber());
        return assetRepo.save(asset);
    }

    @Transactional
    public Asset disposeAsset(Long assetId, String reason, Double disposalValue) {
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        if (asset.getCollateralStatus() == Asset.CollateralStatus.PLEDGED) {
            throw new RuntimeException("Cannot dispose asset that is pledged as collateral");
        }

        asset.setStatus(Asset.AssetStatus.DISPOSED);
        asset.setDisposalDate(LocalDate.now());
        asset.setDisposalReason(reason);
        asset.setDisposalValue(disposalValue);
        asset.setCollateralStatus(Asset.CollateralStatus.NOT_ELIGIBLE);

        log.info("Asset {} disposed. Reason: {}, Value: {}", asset.getAssetNumber(), reason, disposalValue);
        return assetRepo.save(asset);
    }

    // ========== Query Methods ==========

    public List<Asset> getAllAssets() {
        return assetRepo.findAll();
    }

    public Optional<Asset> getAssetById(Long id) {
        return assetRepo.findById(id);
    }

    public Optional<Asset> getAssetByNumber(String assetNumber) {
        return assetRepo.findByAssetNumber(assetNumber);
    }

    public List<Asset> getAssetsByOwner(Long ownerId) {
        return assetRepo.findByOwnerId(ownerId);
    }

    public List<Asset> getAvailableForCollateral() {
        return assetRepo.findAvailableForCollateral();
    }

    public List<Asset> getAssetsForLoan(Long loanAccountId) {
        return assetRepo.findByLinkedLoanAccountId(loanAccountId);
    }

    public Double getTotalAssetValueByOwner(Long ownerId) {
        Double total = assetRepo.getTotalAssetValueByOwner(ownerId);
        return total != null ? total : 0.0;
    }

    public List<Asset> searchAssets(String searchTerm) {
        return assetRepo.searchAssets(searchTerm);
    }

    public List<Asset> getInsuranceExpiringWithin(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return assetRepo.findByInsuranceExpiringBefore(expiryDate);
    }

    public List<Asset> getInspectionsDue() {
        return assetRepo.findByInspectionDueBefore(LocalDate.now());
    }

    /**
     * Generate asset number
     */
    public String generateAssetNumber(Asset.AssetCategory category) {
        String prefix = switch (category) {
            case LAND -> "LAND";
            case BUILDING -> "BLDG";
            case VEHICLE -> "VEH";
            case MACHINERY -> "MACH";
            case EQUIPMENT -> "EQUP";
            case FURNITURE -> "FURN";
            case ELECTRONICS -> "ELEC";
            case LIVESTOCK -> "LVST";
            default -> "ASST";
        };
        
        return prefix + "-" + System.currentTimeMillis();
    }
}
