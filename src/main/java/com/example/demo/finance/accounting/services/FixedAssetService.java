package com.example.demo.finance.accounting.services;

import com.example.demo.finance.accounting.entities.*;
import com.example.demo.finance.accounting.repositories.AssetCategoryRepository;
import com.example.demo.finance.accounting.repositories.FixedAssetRepository;
import com.example.demo.finance.accounting.entities.AssetCategory;
import com.example.demo.finance.accounting.entities.FixedAsset;
import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service("fixedAssetService")
@RequiredArgsConstructor
@Log4j2
public class FixedAssetService {

    private final FixedAssetRepository assetRepo;
    private final AssetCategoryRepository categoryRepo;
    private final AccountingService accountingService;

    @Transactional
    public FixedAsset registerAsset(FixedAsset asset, String createdBy) {
        if (assetRepo.existsByAssetCode(asset.getAssetCode())) {
            throw new RuntimeException("Asset code already exists: " + asset.getAssetCode());
        }

        asset.setCreatedBy(createdBy);
        asset.setStatus(FixedAsset.AssetStatus.ACTIVE);
        asset.setAccumulatedDepreciation(0.0);
        asset.calculateCurrentBookValue();

        // Create journal entry for asset purchase
        JournalEntry journalEntry = createAssetPurchaseJournalEntry(asset, createdBy);
        JournalEntry posted = accountingService.createJournalEntry(journalEntry, createdBy);
        accountingService.postJournalEntry(posted.getId(), createdBy);

        asset.setJournalEntryId(posted.getJournalNumber());

        log.info("Registered asset: {} costing {}", asset.getAssetCode(), asset.getPurchaseCost());
        return assetRepo.save(asset);
    }

    @Transactional
    public FixedAsset updateAsset(Long id, FixedAsset asset) {
        FixedAsset existing = assetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));

        existing.setAssetName(asset.getAssetName());
        existing.setDescription(asset.getDescription());
        existing.setLocation(asset.getLocation());
        existing.setAssignedTo(asset.getAssignedTo());
        existing.setStatus(asset.getStatus());

        return assetRepo.save(existing);
    }

    @Transactional
    public void calculateMonthlyDepreciation(LocalDate month) {
        log.info("Calculating depreciation for month: {}", month);

        List<FixedAsset> activeAssets = assetRepo.findByStatusOrderByAssetCodeAsc(FixedAsset.AssetStatus.ACTIVE);
        
        for (FixedAsset asset : activeAssets) {
            // Skip if asset was purchased this month
            if (asset.getPurchaseDate().isAfter(month) || asset.getPurchaseDate().equals(month)) {
                continue;
            }

            Double monthlyDepreciation = calculateDepreciationAmount(asset);
            
            if (monthlyDepreciation > 0) {
                // Update accumulated depreciation
                asset.setAccumulatedDepreciation(asset.getAccumulatedDepreciation() + monthlyDepreciation);
                asset.calculateCurrentBookValue();

                // Create journal entry
                JournalEntry journalEntry = createDepreciationJournalEntry(asset, monthlyDepreciation, month);
                JournalEntry posted = accountingService.createJournalEntry(journalEntry, "system");
                accountingService.postJournalEntry(posted.getId(), "system");

                assetRepo.save(asset);
                
                log.info("Depreciation for {}: {}", asset.getAssetCode(), monthlyDepreciation);
            }
        }

        log.info("Monthly depreciation calculation completed");
    }

    @Transactional
    public FixedAsset disposeAsset(Long id, Double disposalValue, LocalDate disposalDate, String disposedBy) {
        FixedAsset asset = assetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));

        if (asset.getStatus() != FixedAsset.AssetStatus.ACTIVE) {
            throw new RuntimeException("Only active assets can be disposed");
        }

        asset.setStatus(FixedAsset.AssetStatus.DISPOSED);
        asset.setDisposalDate(disposalDate);
        asset.setDisposalValue(disposalValue != null ? disposalValue : 0.0);

        // Create journal entry for disposal
        JournalEntry journalEntry = createAssetDisposalJournalEntry(asset, disposedBy);
        JournalEntry posted = accountingService.createJournalEntry(journalEntry, disposedBy);
        accountingService.postJournalEntry(posted.getId(), disposedBy);

        log.info("Asset {} disposed. Book value: {}, Disposal value: {}", 
                 asset.getAssetCode(), asset.getCurrentBookValue(), asset.getDisposalValue());
        
        return assetRepo.save(asset);
    }

    public List<FixedAsset> getActiveAssets() {
        return assetRepo.findByStatusOrderByAssetCodeAsc(FixedAsset.AssetStatus.ACTIVE);
    }

    public List<FixedAsset> getAllAssets() {
        return assetRepo.findAll();
    }

    public Double getTotalAssetValue() {
        return assetRepo.getTotalAssetValue();
    }

    // ========== Asset Categories ==========

    @Transactional
    public AssetCategory createCategory(AssetCategory category) {
        if (categoryRepo.existsByCode(category.getCode())) {
            throw new RuntimeException("Category code already exists: " + category.getCode());
        }
        return categoryRepo.save(category);
    }

    public List<AssetCategory> getAllCategories() {
        return categoryRepo.findByIsActiveTrueOrderByNameAsc();
    }

    @Transactional
    public void initializeStandardCategories() {
        if (categoryRepo.count() > 0) {
            log.info("Asset categories already initialized");
            return;
        }

        log.info("Initializing standard asset categories...");

        createStandardCategory("ASSET-FURN", "Furniture & Fixtures", "Office furniture", 
                              "1051", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 12.5, 8);
        createStandardCategory("ASSET-COMP", "Computer Equipment", "Computers and IT equipment", 
                              "1052", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 25.0, 4);
        createStandardCategory("ASSET-OFFC", "Office Equipment", "Printers, scanners, etc", 
                              "1053", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 20.0, 5);
        createStandardCategory("ASSET-VEH", "Vehicles", "Company vehicles", 
                              "1054", FixedAsset.DepreciationMethod.DECLINING_BALANCE, 25.0, 4);
        createStandardCategory("ASSET-BUILD", "Buildings", "Office buildings", 
                              "1055", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 2.0, 50);

        log.info("Standard asset categories initialized");
    }

    private void createStandardCategory(String code, String name, String description, 
                                       String accountCode, FixedAsset.DepreciationMethod method,
                                       Double rate, Integer usefulLife) {
        AssetCategory category = AssetCategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .accountCode(accountCode)
                .defaultDepreciationMethod(method)
                .defaultDepreciationRate(rate)
                .defaultUsefulLife(usefulLife)
                .isActive(true)
                .build();
        categoryRepo.save(category);
    }

    // ========== Helper Methods ==========

    private Double calculateDepreciationAmount(FixedAsset asset) {
        if (asset.getUsefulLifeYears() == null || asset.getUsefulLifeYears() <= 0) {
            return 0.0;
        }

        Double depreciableAmount = asset.getPurchaseCost() - asset.getResidualValue();

        if (asset.getDepreciationMethod() == FixedAsset.DepreciationMethod.STRAIGHT_LINE) {
            // Monthly straight-line depreciation
            return depreciableAmount / (asset.getUsefulLifeYears() * 12);
        } else if (asset.getDepreciationMethod() == FixedAsset.DepreciationMethod.DECLINING_BALANCE) {
            // Monthly declining balance
            Double rate = asset.getDepreciationRate() != null ? asset.getDepreciationRate() / 100 : 0.20;
            Double monthlyRate = rate / 12;
            return asset.getCurrentBookValue() * monthlyRate;
        }

        return 0.0;
    }

    private JournalEntry createAssetPurchaseJournalEntry(FixedAsset asset, String createdBy) {
        JournalEntry entry = JournalEntry.builder()
                .transactionDate(asset.getPurchaseDate())
                .description("Asset Purchase: " + asset.getAssetName())
                .reference(asset.getAssetCode())
                .journalType(JournalEntry.JournalType.PURCHASES)
                .createdBy(createdBy)
                .build();

        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit: Fixed Asset Account
        String assetAccountCode = asset.getCategory().getAccountCode() != null ? 
                                   asset.getCategory().getAccountCode() : "1050";
        
        JournalEntryLine debitLine = JournalEntryLine.builder()
                .accountCode(assetAccountCode)
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(asset.getPurchaseCost())
                .description("Purchase: " + asset.getAssetName())
                .lineNumber(1)
                .build();
        lines.add(debitLine);

        // Credit: Cash/Bank Account
        JournalEntryLine creditLine = JournalEntryLine.builder()
                .accountCode("1020") // Bank account
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(asset.getPurchaseCost())
                .description("Payment for: " + asset.getAssetName())
                .lineNumber(2)
                .build();
        lines.add(creditLine);

        entry.setLines(lines);
        return entry;
    }

    private JournalEntry createDepreciationJournalEntry(FixedAsset asset, Double amount, LocalDate month) {
        JournalEntry entry = JournalEntry.builder()
                .transactionDate(month.withDayOfMonth(month.lengthOfMonth())) // Last day of month
                .description("Monthly Depreciation: " + asset.getAssetName())
                .reference("DEP-" + asset.getAssetCode() + "-" + month.toString())
                .journalType(JournalEntry.JournalType.ADJUSTMENT)
                .createdBy("system")
                .build();

        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit: Depreciation Expense
        JournalEntryLine debitLine = JournalEntryLine.builder()
                .accountCode("5130") // Depreciation expense
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(amount)
                .description("Depreciation: " + asset.getAssetName())
                .lineNumber(1)
                .build();
        lines.add(debitLine);

        // Credit: Accumulated Depreciation
        JournalEntryLine creditLine = JournalEntryLine.builder()
                .accountCode("1059") // Accumulated depreciation
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(amount)
                .description("Accumulated: " + asset.getAssetName())
                .lineNumber(2)
                .build();
        lines.add(creditLine);

        entry.setLines(lines);
        return entry;
    }

    private JournalEntry createAssetDisposalJournalEntry(FixedAsset asset, String disposedBy) {
        JournalEntry entry = JournalEntry.builder()
                .transactionDate(asset.getDisposalDate())
                .description("Asset Disposal: " + asset.getAssetName())
                .reference("DISP-" + asset.getAssetCode())
                .journalType(JournalEntry.JournalType.GENERAL)
                .createdBy(disposedBy)
                .build();

        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit: Accumulated Depreciation
        JournalEntryLine debitAccDep = JournalEntryLine.builder()
                .accountCode("1059")
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(asset.getAccumulatedDepreciation())
                .description("Clear accumulated depreciation")
                .lineNumber(1)
                .build();
        lines.add(debitAccDep);

        // Debit/Credit: Cash (if disposal value exists)
        if (asset.getDisposalValue() > 0) {
            JournalEntryLine debitCash = JournalEntryLine.builder()
                    .accountCode("1020")
                    .type(JournalEntryLine.EntryType.DEBIT)
                    .amount(asset.getDisposalValue())
                    .description("Cash from disposal")
                    .lineNumber(2)
                    .build();
            lines.add(debitCash);
        }

        // Debit/Credit: Loss/Gain on Disposal
        Double bookValue = asset.getCurrentBookValue();
        Double disposalValue = asset.getDisposalValue();
        Double difference = disposalValue - bookValue;

        if (difference > 0) {
            // Gain on disposal
            JournalEntryLine creditGain = JournalEntryLine.builder()
                    .accountCode("4990") // Gain on disposal
                    .type(JournalEntryLine.EntryType.CREDIT)
                    .amount(Math.abs(difference))
                    .description("Gain on disposal")
                    .lineNumber(3)
                    .build();
            lines.add(creditGain);
        } else if (difference < 0) {
            // Loss on disposal
            JournalEntryLine debitLoss = JournalEntryLine.builder()
                    .accountCode("5990") // Loss on disposal
                    .type(JournalEntryLine.EntryType.DEBIT)
                    .amount(Math.abs(difference))
                    .description("Loss on disposal")
                    .lineNumber(3)
                    .build();
            lines.add(debitLoss);
        }

        // Credit: Fixed Asset Account (original cost)
        String assetAccountCode = asset.getCategory().getAccountCode() != null ? 
                                   asset.getCategory().getAccountCode() : "1050";
        
        JournalEntryLine creditAsset = JournalEntryLine.builder()
                .accountCode(assetAccountCode)
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(asset.getPurchaseCost())
                .description("Remove asset from books")
                .lineNumber(4)
                .build();
        lines.add(creditAsset);

        entry.setLines(lines);
        return entry;
    }
}
