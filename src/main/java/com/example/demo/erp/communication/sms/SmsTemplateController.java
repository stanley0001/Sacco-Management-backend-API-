package com.example.demo.erp.communication.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/communication/templates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SmsTemplateController {

    private final SmsTemplateService templateService;

    /**
     * Get all templates
     */
    @GetMapping
    public ResponseEntity<List<SmsTemplate>> getAllTemplates() {
        log.info("GET /api/communication/templates - Fetching all templates");
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    /**
     * Get active templates
     */
    @GetMapping("/active")
    public ResponseEntity<List<SmsTemplate>> getActiveTemplates() {
        log.info("GET /api/communication/templates/active - Fetching active templates");
        return ResponseEntity.ok(templateService.getActiveTemplates());
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SmsTemplate> getTemplateById(@PathVariable Long id) {
        log.info("GET /api/communication/templates/{} - Fetching template", id);
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    /**
     * Get template by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<SmsTemplate> getTemplateByCode(@PathVariable String code) {
        log.info("GET /api/communication/templates/code/{} - Fetching template by code", code);
        return ResponseEntity.ok(templateService.getTemplateByCode(code));
    }

    /**
     * Get templates by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SmsTemplate>> getTemplatesByCategory(@PathVariable String category) {
        log.info("GET /api/communication/templates/category/{} - Fetching templates", category);
        return ResponseEntity.ok(templateService.getTemplatesByCategory(category));
    }

    /**
     * Get available categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.info("GET /api/communication/templates/categories - Fetching categories");
        return ResponseEntity.ok(templateService.getCategories());
    }

    /**
     * Create new template
     */
    @PostMapping
    public ResponseEntity<SmsTemplate> createTemplate(@RequestBody SmsTemplate template) {
        log.info("POST /api/communication/templates - Creating template: {}", template.getName());
        return ResponseEntity.ok(templateService.createTemplate(template));
    }

    /**
     * Update template
     */
    @PutMapping("/{id}")
    public ResponseEntity<SmsTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody SmsTemplate template) {
        log.info("PUT /api/communication/templates/{} - Updating template", id);
        return ResponseEntity.ok(templateService.updateTemplate(id, template));
    }

    /**
     * Delete template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("DELETE /api/communication/templates/{} - Deleting template", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Toggle template active status
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<SmsTemplate> toggleActive(@PathVariable Long id) {
        log.info("PATCH /api/communication/templates/{}/toggle - Toggling active status", id);
        return ResponseEntity.ok(templateService.toggleActive(id));
    }

    /**
     * Get populated message from template
     */
    @PostMapping("/populate/{code}")
    public ResponseEntity<Map<String, String>> getPopulatedMessage(
            @PathVariable String code,
            @RequestBody Map<String, String> variables) {
        log.info("POST /api/communication/templates/populate/{} - Populating message", code);
        String message = templateService.getPopulatedMessage(code, variables);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
