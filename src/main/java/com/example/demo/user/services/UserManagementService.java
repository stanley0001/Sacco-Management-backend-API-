package com.example.demo.user.services;

import com.example.demo.user.entities.UserProfile;
import com.example.demo.user.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users (admins see all, others see branch-specific)
     */
    public List<UserProfile> getAllUsers(String currentUser) {
        // For now, return all users - implement branch filtering based on user role
        return userProfileRepository.findAll();
    }

    /**
     * Create new user
     */
    @Transactional
    public UserProfile createUser(UserProfile userProfile, String createdBy) {
        // Check if username or email already exists
        if (userProfileRepository.findByUsername(userProfile.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + userProfile.getUsername());
        }
        
        if (userProfileRepository.findByEmail(userProfile.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + userProfile.getEmail());
        }

        // Encode password
        if (userProfile.getPassword() != null && !userProfile.getPassword().isEmpty()) {
            userProfile.setPassword(passwordEncoder.encode(userProfile.getPassword()));
        }

        // Set audit fields
        userProfile.setCreatedBy(createdBy);
        userProfile.setCreatedAt(LocalDateTime.now());
        
        // Set default status if not provided
        if (userProfile.getStatus() == null) {
            userProfile.setStatus(UserProfile.UserStatus.PENDING_ACTIVATION);
        }

        return userProfileRepository.save(userProfile);
    }

    /**
     * Update user
     */
    @Transactional
    public UserProfile updateUser(Long id, UserProfile updatedProfile, String updatedBy) {
        UserProfile existingUser = userProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Check if new username/email conflicts with existing ones
        if (!existingUser.getUsername().equals(updatedProfile.getUsername()) &&
            userProfileRepository.findByUsername(updatedProfile.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + updatedProfile.getUsername());
        }

        if (!existingUser.getEmail().equals(updatedProfile.getEmail()) &&
            userProfileRepository.findByEmail(updatedProfile.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + updatedProfile.getEmail());
        }

        // Update fields
        existingUser.setFirstName(updatedProfile.getFirstName());
        existingUser.setLastName(updatedProfile.getLastName());
        existingUser.setEmail(updatedProfile.getEmail());
        existingUser.setPhoneNumber(updatedProfile.getPhoneNumber());
        existingUser.setUserType(updatedProfile.getUserType());
        existingUser.setBranchId(updatedProfile.getBranchId());
        existingUser.setEmployeeId(updatedProfile.getEmployeeId());
        existingUser.setDepartment(updatedProfile.getDepartment());
        existingUser.setPosition(updatedProfile.getPosition());

        // Update password if provided
        if (updatedProfile.getPassword() != null && !updatedProfile.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedProfile.getPassword()));
        }

        // Update audit fields
        existingUser.setUpdatedBy(updatedBy);
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(existingUser);
    }

    /**
     * Toggle user status
     */
    @Transactional
    public UserProfile toggleUserStatus(Long id, Boolean isActive, String updatedBy) {
        UserProfile user = userProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        UserProfile.UserStatus newStatus = isActive ? 
            UserProfile.UserStatus.ACTIVE : UserProfile.UserStatus.INACTIVE;
        
        user.setStatus(newStatus);
        user.setUpdatedBy(updatedBy);
        user.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(user);
    }

    /**
     * Delete user (soft delete by deactivating)
     */
    @Transactional
    public void deleteUser(Long id, String deletedBy) {
        UserProfile user = userProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setStatus(UserProfile.UserStatus.INACTIVE);
        user.setUpdatedBy(deletedBy);
        user.setUpdatedAt(LocalDateTime.now());

        userProfileRepository.save(user);
    }

    /**
     * Assign user to branch
     */
    @Transactional
    public UserProfile assignUserToBranch(Long userId, Long branchId, String updatedBy) {
        UserProfile user = userProfileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setBranchId(branchId);
        user.setUpdatedBy(updatedBy);
        user.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(user);
    }

    /**
     * Assign user as loan officer
     */
    @Transactional
    public UserProfile assignAsLoanOfficer(Long userId, Long branchId, String updatedBy) {
        UserProfile user = userProfileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setUserType(UserProfile.UserType.LOAN_OFFICER);
        user.setBranchId(branchId);
        user.setUpdatedBy(updatedBy);
        user.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(user);
    }

    /**
     * Get users by branch
     */
    public List<UserProfile> getUsersByBranch(Long branchId) {
        return userProfileRepository.findByBranchId(branchId);
    }

    /**
     * Get all loan officers
     */
    public List<UserProfile> getLoanOfficers() {
        return userProfileRepository.findByUserType(UserProfile.UserType.LOAN_OFFICER);
    }

    /**
     * Check if user can be deleted
     */
    public boolean canDeleteUser(Long userId, String currentUser) {
        // Don't allow deletion of system admin or self
        UserProfile user = userProfileRepository.findById(userId).orElse(null);
        if (user == null) return false;
        
        // Don't delete system admin
        if (user.getId() == 1 || "admin@helasuite.com".equals(user.getEmail())) {
            return false;
        }
        
        // Don't delete self
        return !user.getUsername().equals(currentUser);
    }
}
