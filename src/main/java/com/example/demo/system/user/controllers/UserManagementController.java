package com.example.demo.system.user.controllers;

import com.example.demo.system.userManagements.parsitence.enitities.Users;
import com.example.demo.system.userManagements.parsitence.enitities.Roles;
import com.example.demo.system.userManagements.parsitence.enitities.rolePermissions;
import com.example.demo.system.userManagements.serviceImplementation.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Enhanced user management with branch and role support")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class UserManagementController {

    private final UserService userService;

    /**
     * Get all users
     */
    @GetMapping("/all")
    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<List<Users>> getAllUsers(Authentication authentication) {
        try {
            List<Users> users = userService.getAll();
            log.info("Returning {} users from /all endpoint", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Debug endpoint - Get all users with detailed response
     */
    @GetMapping("/debug/all")
    @Operation(summary = "Debug: Get all users with detailed response")
    public ResponseEntity<?> getAllUsersDebug(Authentication authentication) {
        try {
            List<Users> users = userService.getAll();
            log.info("Debug endpoint returning {} users", users.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Users retrieved successfully",
                "count", users.size(),
                "users", users,
                "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Error in debug users endpoint", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Create new user
     */
    @PostMapping("/create")
    @Operation(summary = "Create new user")
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> createUser(
        @RequestBody Users user,
        Authentication authentication
    ) {
        try {
            // Validate required fields
            if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Username is required"
                ));
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Email is required"
                ));
            }
            
            // Set default values
            user.setCreatedAt(java.time.LocalDate.now());
            user.setUpdatedAt(java.time.LocalDate.now());
            if (user.getActive() == null) {
                user.setActive(true);
            }
            
            List<Users> createdUsers = userService.saveUser(user);
            Users createdUser = !createdUsers.isEmpty() ? createdUsers.get(0) : user;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User created successfully",
                "userId", createdUser.getId(),
                "user", createdUser
            ));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to create user: " + e.getMessage()
            ));
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> updateUser(
        @PathVariable Long id,
        @RequestBody Users user,
        Authentication authentication
    ) {
        try {
            // Check if user exists
            Users existingUser = userService.findById(id).orElse(null);
            if (existingUser == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            // Preserve creation date and update modified date
            user.setId(id);
            user.setCreatedAt(existingUser.getCreatedAt());
            user.setUpdatedAt(java.time.LocalDate.now());
            
            // Validate required fields
            if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Username is required"
                ));
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Email is required"
                ));
            }
            
            Users updatedUser = userService.updateUser(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User updated successfully",
                "user", updatedUser
            ));
        } catch (Exception e) {
            log.error("Error updating user {}", id, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to update user: " + e.getMessage()
            ));
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Users user = userService.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user
            ));
        } catch (Exception e) {
            log.error("Error fetching user by ID {}", id, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to fetch user: " + e.getMessage()
            ));
        }
    }

    /**
     * Find user by username
     */
    @GetMapping("/find/{username}")
    @Operation(summary = "Find user by username")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        try {
            Users user = userService.findByName(username).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user
            ));
        } catch (Exception e) {
            log.error("Error finding user by username {}", username, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to find user: " + e.getMessage()
            ));
        }
    }

    /**
     * Toggle user status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle user active status")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> toggleUserStatus(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            Boolean isActive = (Boolean) request.get("status");
            
            // Get user and update status
            Users user = userService.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            user.setActive(isActive);
            Users updatedUser = userService.updateUser(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User status updated successfully",
                "status", updatedUser.getActive()
            ));
        } catch (Exception e) {
            log.error("Error updating user status for {}", id, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to update user status: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    @PreAuthorize("hasAnyAuthority('USER_DELETE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            // Get user and deactivate instead of delete to preserve data integrity
            Users user = userService.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            user.setActive(false);
            userService.updateUser(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting user {}", id, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to delete user: " + e.getMessage()
            ));
        }
    }

    /**
     * Assign user to branch
     */
    @PostMapping("/{id}/assign-branch")
    @Operation(summary = "Assign user to branch")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> assignUserToBranch(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            String branchCode = request.get("branchId").toString();
            
            // Get user and update branch
            Users user = userService.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            user.setBranchCode(branchCode);
            Users updatedUser = userService.updateUser(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User assigned to branch successfully",
                "branchId", updatedUser.getBranchCode()
            ));
        } catch (Exception e) {
            log.error("Error assigning user {} to branch", id, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to assign user to branch: " + e.getMessage()
            ));
        }
    }

    /**
     * Assign user as loan officer
     */
    @PostMapping("/{id}/assign-loan-officer")
    @Operation(summary = "Assign user as loan officer")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> assignAsLoanOfficer(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            String branchCode = request.get("branchId").toString();
            
            // Get user and update as loan officer
            Users user = userService.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }
            
            user.setUserType("LOAN_OFFICER");
            user.setBranchCode(branchCode);
            Users updatedUser = userService.updateUser(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User assigned as loan officer successfully",
                "userType", updatedUser.getUserType(),
                "branchId", updatedUser.getBranchCode()
            ));
        } catch (Exception e) {
            log.error("Error assigning user {} as loan officer", id, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to assign loan officer: " + e.getMessage()
            ));
        }
    }

    /**
     * Get users by branch
     */
    @GetMapping("/branch/{branchCode}")
    @Operation(summary = "Get users by branch")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<List<Users>> getUsersByBranch(@PathVariable String branchCode) {
        try {
            // Filter users by branch code
            List<Users> allUsers = userService.getAll();
            List<Users> branchUsers = allUsers.stream()
                .filter(user -> branchCode.equals(user.getBranchCode()))
                .toList();
            return ResponseEntity.ok(branchUsers);
        } catch (Exception e) {
            log.error("Error fetching users for branch {}", branchCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all loan officers
     */
    @GetMapping("/loan-officers")
    @Operation(summary = "Get all loan officers")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<List<Users>> getLoanOfficers() {
        try {
            // Filter users by loan officer type
            List<Users> allUsers = userService.getAll();
            List<Users> loanOfficers = allUsers.stream()
                .filter(user -> "LOAN_OFFICER".equals(user.getUserType()))
                .toList();
            return ResponseEntity.ok(loanOfficers);
        } catch (Exception e) {
            log.error("Error fetching loan officers", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current logged-in user
     */
    @GetMapping("/current")
    @Operation(summary = "Get current logged-in user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No authenticated user found"
                ));
            }
            
            String username = authentication.getName();
            
            // Find user by username
            List<Users> allUsers = userService.getAll();
            Users currentUser = allUsers.stream()
                .filter(user -> username.equals(user.getUserName()) || username.equals(user.getEmail()))
                .findFirst()
                .orElse(null);
                
            if (currentUser == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "User not found in database"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", currentUser,
                "username", username
            ));
        } catch (Exception e) {
            log.error("Error fetching current user", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to fetch current user: " + e.getMessage()
            ));
        }
    }

    // ============ ROLES & PERMISSIONS ============
    
    /**
     * Get all roles
     */
    @GetMapping("/security/roles")
    @Operation(summary = "Get all roles")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<List<Roles>> getAllRoles() {
        try {
            List<Roles> roles = userService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Error fetching roles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new role
     */
    @PostMapping("/security/createRole")
    @Operation(summary = "Create new role")
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ADMIN_ACCESS')")
    public ResponseEntity<Roles> createRole(@RequestBody Roles role) {
        try {
            Roles createdRole = userService.createRole(role);
            return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating role", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all permissions
     */
    @GetMapping("/security/permissions")
    @Operation(summary = "Get all permissions")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<List<rolePermissions>> getAllPermissions() {
        try {
            List<rolePermissions> permissions = userService.getAllPermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Error fetching permissions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new permission
     */
    @PostMapping("/security/createPermission")
    @Operation(summary = "Create new permission")
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ADMIN_ACCESS')")
    public ResponseEntity<rolePermissions> createPermission(@RequestBody rolePermissions permission) {
        try {
            rolePermissions createdPermission = userService.createPermission(permission);
            return new ResponseEntity<>(createdPermission, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating permission", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
