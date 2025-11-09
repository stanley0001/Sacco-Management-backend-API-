package com.example.demo.system.controllers;

import com.example.demo.system.entities.GlobalConfig;
import com.example.demo.system.repositories.GlobalConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/global-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Global Configuration", description = "System-wide configuration management")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GlobalConfigController {

    private final GlobalConfigRepository globalConfigRepository;

    @GetMapping
    @Operation(summary = "Get all configurations")
    public ResponseEntity<List<GlobalConfig>> getAllConfigs() {
        return ResponseEntity.ok(globalConfigRepository.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active configurations")
    public ResponseEntity<List<GlobalConfig>> getActiveConfigs() {
        return ResponseEntity.ok(globalConfigRepository.findByIsActiveTrue());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get configurations by category")
    public ResponseEntity<List<GlobalConfig>> getConfigsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(globalConfigRepository.findByCategoryAndIsActiveTrue(category));
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get configuration by key")
    public ResponseEntity<GlobalConfig> getConfigByKey(@PathVariable String key) {
        return globalConfigRepository.findByConfigKey(key)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new configuration")
    public ResponseEntity<GlobalConfig> createConfig(@RequestBody GlobalConfig config) {
        log.info("Creating global config: {}", config.getConfigKey());
        return ResponseEntity.ok(globalConfigRepository.save(config));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update configuration")
    public ResponseEntity<GlobalConfig> updateConfig(
        @PathVariable Long id,
        @RequestBody GlobalConfig config
    ) {
        return globalConfigRepository.findById(id)
            .map(existing -> {
                existing.setConfigValue(config.getConfigValue());
                existing.setConfigName(config.getConfigName());
                existing.setDescription(config.getDescription());
                existing.setIsActive(config.getIsActive());
                existing.setUpdatedBy(config.getUpdatedBy());
                log.info("Updated global config: {}", existing.getConfigKey());
                return ResponseEntity.ok(globalConfigRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/key/{key}/value")
    @Operation(summary = "Update configuration value by key")
    public ResponseEntity<GlobalConfig> updateConfigValue(
        @PathVariable String key,
        @RequestBody Map<String, String> payload
    ) {
        String value = payload.get("value");
        String updatedBy = payload.get("updatedBy");

        return globalConfigRepository.findByConfigKey(key)
            .map(config -> {
                config.setConfigValue(value);
                config.setUpdatedBy(updatedBy);
                log.info("Updated config value: {} = {}", key, value);
                return ResponseEntity.ok(globalConfigRepository.save(config));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete configuration")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        if (globalConfigRepository.existsById(id)) {
            globalConfigRepository.deleteById(id);
            log.info("Deleted global config: {}", id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/init-defaults")
    @Operation(summary = "Initialize default configurations")
    public ResponseEntity<String> initializeDefaults() {
        log.info("Initializing default global configurations");

        // Auto Loan Deduction
        createIfNotExists("AUTO_LOAN_DEDUCTION_ENABLED", "Auto Loan Deduction",
            "Enable automatic loan deduction from deposits", "false",
            GlobalConfig.ConfigType.BOOLEAN, "STANDING_ORDER");

        createIfNotExists("DEFAULT_DEDUCTION_PERCENTAGE", "Default Deduction Percentage",
            "Default percentage to deduct from deposits (0-100)", "50",
            GlobalConfig.ConfigType.DECIMAL, "STANDING_ORDER");

        createIfNotExists("MINIMUM_BALANCE_AFTER_DEDUCTION", "Minimum Balance After Deduction",
            "Minimum balance to maintain after deduction (KES)", "100",
            GlobalConfig.ConfigType.DECIMAL, "STANDING_ORDER");

        // Approval Workflow
        createIfNotExists("APPROVAL_WORKFLOW_ENABLED", "Approval Workflow Enabled",
            "Enable multi-level approval workflow", "false",
            GlobalConfig.ConfigType.BOOLEAN, "APPROVAL_WORKFLOW");

        createIfNotExists("AUTO_APPROVE_TIMEOUT_HOURS", "Auto Approve Timeout",
            "Hours before auto-approval (0 = disabled)", "0",
            GlobalConfig.ConfigType.INTEGER, "APPROVAL_WORKFLOW");

        createIfNotExists("SEND_SMS_ON_APPROVAL_LEVEL", "SMS on Approval Level",
            "Send SMS when loan reaches approval level", "true",
            GlobalConfig.ConfigType.BOOLEAN, "APPROVAL_WORKFLOW");

        // Loan Settings
        createIfNotExists("MAX_LOAN_AMOUNT", "Maximum Loan Amount",
            "Maximum loan amount allowed (KES)", "1000000",
            GlobalConfig.ConfigType.DECIMAL, "LOAN_SETTINGS");

        createIfNotExists("MIN_LOAN_AMOUNT", "Minimum Loan Amount",
            "Minimum loan amount allowed (KES)", "1000",
            GlobalConfig.ConfigType.DECIMAL, "LOAN_SETTINGS");

        createIfNotExists("DEFAULT_INTEREST_RATE", "Default Interest Rate",
            "Default interest rate percentage", "12",
            GlobalConfig.ConfigType.DECIMAL, "LOAN_SETTINGS");

        log.info("✅ Default configurations initialized");
        return ResponseEntity.ok("Default configurations initialized successfully");
    }

    private void createIfNotExists(String key, String name, String description, String value,
                                   GlobalConfig.ConfigType type, String category) {
        if (globalConfigRepository.findByConfigKey(key).isEmpty()) {
            GlobalConfig config = GlobalConfig.builder()
                .configKey(key)
                .configName(name)
                .description(description)
                .configValue(value)
                .configType(type)
                .isActive(true)
                .category(category)
                .build();
            globalConfigRepository.save(config);
            log.info("Created config: {}", key);
        }
    }
}
