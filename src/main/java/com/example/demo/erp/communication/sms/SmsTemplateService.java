package com.example.demo.erp.communication.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsTemplateService {

    private final SmsTemplateRepository templateRepository;

    /**
     * Get all templates
     */
    public List<SmsTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    /**
     * Get active templates
     */
    public List<SmsTemplate> getActiveTemplates() {
        return templateRepository.findByActive(true);
    }

    /**
     * Get template by ID
     */
    public SmsTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + id));
    }

    /**
     * Get template by code
     */
    public SmsTemplate getTemplateByCode(String code) {
        return templateRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Template not found with code: " + code));
    }

    /**
     * Get templates by category
     */
    public List<SmsTemplate> getTemplatesByCategory(String category) {
        return templateRepository.findByCategory(category);
    }

    /**
     * Create new template
     */
    @Transactional
    public SmsTemplate createTemplate(SmsTemplate template) {
        if (templateRepository.existsByCode(template.getCode())) {
            throw new RuntimeException("Template with code " + template.getCode() + " already exists");
        }

        // Extract variables from message
        List<String> variables = SmsTemplate.extractVariables(template.getMessage());
        template.setVariables(variables);

        return templateRepository.save(template);
    }

    /**
     * Update template
     */
    @Transactional
    public SmsTemplate updateTemplate(Long id, SmsTemplate templateDetails) {
        SmsTemplate template = getTemplateById(id);

        template.setName(templateDetails.getName());
        template.setMessage(templateDetails.getMessage());
        template.setCategory(templateDetails.getCategory());
        template.setDescription(templateDetails.getDescription());
        template.setActive(templateDetails.isActive());

        // Extract and update variables
        List<String> variables = SmsTemplate.extractVariables(templateDetails.getMessage());
        template.setVariables(variables);

        return templateRepository.save(template);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        SmsTemplate template = getTemplateById(id);
        templateRepository.delete(template);
    }

    /**
     * Toggle template active status
     */
    @Transactional
    public SmsTemplate toggleActive(Long id) {
        SmsTemplate template = getTemplateById(id);
        template.setActive(!template.isActive());
        return templateRepository.save(template);
    }

    /**
     * Get message from template with populated variables
     */
    public String getPopulatedMessage(String templateCode, Map<String, String> variables) {
        SmsTemplate template = getTemplateByCode(templateCode);
        return template.populateMessage(variables);
    }

    /**
     * Get available categories
     */
    public List<String> getCategories() {
        return Arrays.asList("LOAN", "PAYMENT", "REMINDER", "NOTIFICATION", "GENERAL", "ALERT");
    }

    /**
     * Seed default templates on startup
     */
    @PostConstruct
    public void seedDefaultTemplates() {
        if (templateRepository.count() == 0) {
            log.info("Seeding default SMS templates...");

            createDefaultTemplate("LOAN_APPLICATION_RECEIVED", "Loan Application Received",
                    "Dear {customerName}, your loan application for KES {amount} has been received. " +
                            "Application Ref: {applicationId}. We will notify you once processed.",
                    "LOAN");

            createDefaultTemplate("LOAN_APPROVED", "Loan Approved",
                    "Congratulations {customerName}! Your loan of KES {amount} has been APPROVED. " +
                            "It will be disbursed shortly. Ref: {loanRef}",
                    "LOAN");

            createDefaultTemplate("LOAN_REJECTED", "Loan Rejected",
                    "Dear {customerName}, we regret to inform you that your loan application for KES {amount} " +
                            "has been declined. Reason: {reason}. Contact us for assistance.",
                    "LOAN");

            createDefaultTemplate("LOAN_DISBURSED", "Loan Disbursed",
                    "Dear {customerName}, your loan of KES {amount} has been disbursed via {method}. " +
                            "Loan Ref: {loanRef}. Total payable: KES {totalAmount}. Thank you.",
                    "LOAN");

            createDefaultTemplate("PAYMENT_RECEIVED", "Payment Received",
                    "Dear {customerName}, we have received your payment of KES {amount}. " +
                            "Outstanding balance: KES {balance}. Receipt: {receipt}. Thank you.",
                    "PAYMENT");

            createDefaultTemplate("PAYMENT_REMINDER", "Payment Reminder",
                    "Payment Reminder: Dear {customerName}, your payment of KES {amount} is due on {dueDate}. " +
                            "Loan Ref: {loanRef}. Please pay on time to avoid penalties.",
                    "REMINDER");

            createDefaultTemplate("PAYMENT_OVERDUE", "Payment Overdue",
                    "OVERDUE NOTICE: Dear {customerName}, your payment of KES {amount} is {days} days overdue. " +
                            "Loan Ref: {loanRef}. Please pay immediately to avoid penalties.",
                    "ALERT");

            createDefaultTemplate("ACCOUNT_CREATED", "Account Created",
                    "Welcome {customerName}! Your account has been created successfully. " +
                            "Account Number: {accountNumber}. Thank you for choosing us.",
                    "NOTIFICATION");

            log.info("✅ Default SMS templates seeded successfully");
        }
    }

    private void createDefaultTemplate(String code, String name, String message, String category) {
        if (!templateRepository.existsByCode(code)) {
            SmsTemplate template = new SmsTemplate();
            template.setCode(code);
            template.setName(name);
            template.setMessage(message);
            template.setCategory(category);
            template.setActive(true);
            template.setCreatedBy("system");
            template.setDescription("Default system template");

            List<String> variables = SmsTemplate.extractVariables(message);
            template.setVariables(variables);

            templateRepository.save(template);
        }
    }
}
