package com.example.demo.erp.branch.controllers;

import com.example.demo.erp.branch.entities.Branch;
import com.example.demo.erp.branch.services.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branch Management", description = "Manage organizational branches")
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/all")
    @Operation(summary = "Get all branches")
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active branches")
    public ResponseEntity<List<Branch>> getActiveBranches() {
        return ResponseEntity.ok(branchService.getActiveBranches());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<Branch> getBranchById(@PathVariable Long id) {
        return branchService.getBranchById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{branchCode}")
    @Operation(summary = "Get branch by code")
    public ResponseEntity<Branch> getBranchByCode(@PathVariable String branchCode) {
        return branchService.getBranchByCode(branchCode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    @Operation(summary = "Create new branch")
    public ResponseEntity<Map<String, Object>> createBranch(
            @RequestBody Branch branch,
            @RequestParam(required = false, defaultValue = "SYSTEM") String createdBy) {
        try {
            Branch savedBranch = branchService.createBranch(branch, createdBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Branch created successfully",
                "branch", savedBranch
            ));
        } catch (RuntimeException e) {
            log.error("Error creating branch", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update branch")
    public ResponseEntity<Map<String, Object>> updateBranch(
            @PathVariable Long id,
            @RequestBody Branch branch,
            @RequestParam(required = false, defaultValue = "SYSTEM") String updatedBy) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branch, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Branch updated successfully",
                "branch", updatedBranch
            ));
        } catch (RuntimeException e) {
            log.error("Error updating branch", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Activate/Deactivate branch")
    public ResponseEntity<Map<String, Object>> toggleBranchStatus(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "SYSTEM") String updatedBy) {
        try {
            Branch branch = branchService.toggleBranchStatus(id, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Branch status updated successfully",
                "branch", branch
            ));
        } catch (RuntimeException e) {
            log.error("Error toggling branch status", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete branch (soft delete)")
    public ResponseEntity<Map<String, Object>> deleteBranch(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "SYSTEM") String deletedBy) {
        try {
            branchService.deleteBranch(id, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Branch deleted successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error deleting branch", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}/accessible")
    @Operation(summary = "Get branches accessible by user")
    public ResponseEntity<List<Branch>> getUserAccessibleBranches(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(branchService.getUserAccessibleBranches(userId, userRole));
    }
}
