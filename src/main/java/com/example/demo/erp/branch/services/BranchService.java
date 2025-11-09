package com.example.demo.erp.branch.services;

import com.example.demo.erp.branch.entities.Branch;
import com.example.demo.erp.branch.repositories.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final BranchRepository branchRepository;

    /**
     * Get all branches
     */
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    /**
     * Get all active branches
     */
    public List<Branch> getActiveBranches() {
        return branchRepository.findByIsActiveTrue();
    }

    /**
     * Get branch by ID
     */
    public Optional<Branch> getBranchById(Long id) {
        return branchRepository.findById(id);
    }

    /**
     * Get branch by code
     */
    public Optional<Branch> getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode);
    }

    /**
     * Create new branch
     */
    @Transactional
    public Branch createBranch(Branch branch, String createdBy) {
        // Check if branch code already exists
        if (branchRepository.findByBranchCode(branch.getBranchCode()).isPresent()) {
            throw new RuntimeException("Branch code already exists: " + branch.getBranchCode());
        }

        branch.setCreatedBy(createdBy);
        branch.setCreatedAt(LocalDateTime.now());
        branch.setIsActive(true);

        return branchRepository.save(branch);
    }

    /**
     * Update branch
     */
    @Transactional
    public Branch updateBranch(Long id, Branch updatedBranch, String updatedBy) {
        Branch existingBranch = branchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + id));

        // Check if new branch code conflicts with existing ones
        Optional<Branch> branchWithCode = branchRepository.findByBranchCode(updatedBranch.getBranchCode());
        if (branchWithCode.isPresent() && !branchWithCode.get().getId().equals(id)) {
            throw new RuntimeException("Branch code already exists: " + updatedBranch.getBranchCode());
        }

        existingBranch.setBranchCode(updatedBranch.getBranchCode());
        existingBranch.setBranchName(updatedBranch.getBranchName());
        existingBranch.setAddress(updatedBranch.getAddress());
        existingBranch.setPhoneNumber(updatedBranch.getPhoneNumber());
        existingBranch.setEmail(updatedBranch.getEmail());
        existingBranch.setManagerName(updatedBranch.getManagerName());
        existingBranch.setUpdatedBy(updatedBy);
        existingBranch.setUpdatedAt(LocalDateTime.now());

        return branchRepository.save(existingBranch);
    }

    /**
     * Activate/Deactivate branch
     */
    @Transactional
    public Branch toggleBranchStatus(Long id, String updatedBy) {
        Branch branch = branchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + id));

        branch.setIsActive(!branch.getIsActive());
        branch.setUpdatedBy(updatedBy);
        branch.setUpdatedAt(LocalDateTime.now());

        return branchRepository.save(branch);
    }

    /**
     * Delete branch (soft delete by deactivating)
     */
    @Transactional
    public void deleteBranch(Long id, String updatedBy) {
        Branch branch = branchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found: " + id));

        branch.setIsActive(false);
        branch.setUpdatedBy(updatedBy);
        branch.setUpdatedAt(LocalDateTime.now());

        branchRepository.save(branch);
    }

    /**
     * Check if user has access to branch data
     */
    public boolean hasAccessToBranch(Long userId, Long branchId) {
        // Implementation depends on your user-branch relationship
        // For now, return true (will be implemented with user management)
        return true;
    }

    /**
     * Get branches accessible by user
     */
    public List<Branch> getUserAccessibleBranches(Long userId, String userRole) {
        if ("ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            // Admins see all branches
            return getAllBranches();
        } else {
            // Regular users see only their assigned branch
            // This will be implemented with user-branch relationship
            return getActiveBranches();
        }
    }
}
