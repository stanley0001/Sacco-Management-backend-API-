package com.example.demo.erp.communication.sms.services;

import com.example.demo.erp.communication.sms.dto.SmsConfigDTO;
import com.example.demo.erp.communication.sms.entities.SmsConfig;
import com.example.demo.erp.communication.sms.repositories.SmsConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsConfigService {

    private final SmsConfigRepository smsConfigRepository;

    public List<SmsConfigDTO> getAllConfigurations(boolean maskSensitive) {
        return smsConfigRepository.findAll().stream()
            .map(config -> SmsConfigDTO.fromEntity(config, maskSensitive))
            .toList();
    }

    public SmsConfigDTO getConfigurationById(Long id, boolean maskSensitive) {
        SmsConfig config = smsConfigRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("SMS configuration not found with ID: " + id));
        return SmsConfigDTO.fromEntity(config, maskSensitive);
    }

    public SmsConfig getConfigurationEntity(Long id) {
        return smsConfigRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("SMS configuration not found with ID: " + id));
    }

    public SmsConfig getActiveConfiguration() {
        Optional<SmsConfig> activeDefault = smsConfigRepository.findByActiveTrueAndDefaultConfigTrue();
        if (activeDefault.isPresent()) {
            return activeDefault.get();
        }

        return smsConfigRepository.findAll().stream()
            .filter(SmsConfig::getActive)
            .findFirst()
            .orElse(null);
    }

    @Transactional
    public SmsConfigDTO createConfiguration(SmsConfigDTO dto, String currentUser) {
        validateConfigurationName(dto.getConfigName(), null);

        SmsConfig config = new SmsConfig();
        updateEntityFromDTO(config, dto);
        config.setCreatedBy(currentUser);
        config.setUpdatedBy(currentUser);

        if (Boolean.TRUE.equals(config.getDefaultConfig())) {
            clearExistingDefault(null);
        }

        SmsConfig saved = smsConfigRepository.save(config);
        log.info("SMS configuration created with ID {}", saved.getId());
        return SmsConfigDTO.fromEntity(saved, true);
    }

    @Transactional
    public SmsConfigDTO updateConfiguration(Long id, SmsConfigDTO dto, String currentUser) {
        SmsConfig config = getConfigurationEntity(id);
        validateConfigurationName(dto.getConfigName(), id);

        updateEntityFromDTO(config, dto);
        config.setUpdatedBy(currentUser);

        if (Boolean.TRUE.equals(config.getDefaultConfig())) {
            clearExistingDefault(id);
        }

        SmsConfig saved = smsConfigRepository.save(config);
        log.info("SMS configuration updated: {}", id);
        return SmsConfigDTO.fromEntity(saved, true);
    }

    @Transactional
    public void deleteConfiguration(Long id) {
        SmsConfig config = getConfigurationEntity(id);
        if (Boolean.TRUE.equals(config.getDefaultConfig())) {
            throw new IllegalStateException("Cannot delete default SMS configuration. Set another configuration as default first.");
        }
        smsConfigRepository.delete(config);
        log.info("SMS configuration deleted: {}", id);
    }

    @Transactional
    public SmsConfigDTO toggleActiveStatus(Long id) {
        SmsConfig config = getConfigurationEntity(id);
        config.setActive(!Boolean.TRUE.equals(config.getActive()));
        SmsConfig saved = smsConfigRepository.save(config);
        return SmsConfigDTO.fromEntity(saved, true);
    }

    @Transactional
    public SmsConfigDTO setAsDefault(Long id) {
        SmsConfig config = getConfigurationEntity(id);
        clearExistingDefault(id);
        config.setDefaultConfig(true);
        SmsConfig saved = smsConfigRepository.save(config);
        return SmsConfigDTO.fromEntity(saved, true);
    }

    private void validateConfigurationName(String configName, Long excludeId) {
        smsConfigRepository.findByConfigNameIgnoreCase(configName)
            .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("SMS configuration with name '" + configName + "' already exists");
            });
    }

    private void clearExistingDefault(Long excludeId) {
        smsConfigRepository.findAll().forEach(config -> {
            if (Boolean.TRUE.equals(config.getDefaultConfig()) && (excludeId == null || !config.getId().equals(excludeId))) {
                config.setDefaultConfig(false);
                smsConfigRepository.save(config);
            }
        });
    }

    private void updateEntityFromDTO(SmsConfig config, SmsConfigDTO dto) {
        config.setConfigName(dto.getConfigName());
        if (dto.getProviderType() != null) {
            config.setProviderType(SmsConfig.SmsProviderType.valueOf(dto.getProviderType()));
        }
        config.setActive(dto.getActive() != null ? dto.getActive() : Boolean.TRUE);
        config.setDefaultConfig(dto.getDefaultConfig() != null ? dto.getDefaultConfig() : Boolean.FALSE);
        config.setDescription(dto.getDescription());
        config.setApiUrl(dto.getApiUrl());
        if (dto.getApiKey() != null) {
            config.setApiKey(dto.getApiKey());
        }
        config.setUsername(dto.getUsername());
        config.setPartnerId(dto.getPartnerId());
        config.setShortcode(dto.getShortcode());
        config.setSenderId(dto.getSenderId());
        if (dto.getAuthToken() != null) {
            config.setAuthToken(dto.getAuthToken());
        }
        config.setTemplate(dto.getTemplate());
    }
}
