package com.example.demo.user.repositories;

import com.example.demo.user.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Find user by username
     */
    Optional<UserProfile> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<UserProfile> findByEmail(String email);
    
    /**
     * Find users by branch ID
     */
    List<UserProfile> findByBranchId(Long branchId);
    
    /**
     * Find users by user type
     */
    List<UserProfile> findByUserType(UserProfile.UserType userType);
    
    /**
     * Find active users
     */
    List<UserProfile> findByStatus(UserProfile.UserStatus status);
    
    /**
     * Find users by branch and type
     */
    List<UserProfile> findByBranchIdAndUserType(Long branchId, UserProfile.UserType userType);
    
    /**
     * Find active loan officers
     */
    @Query("SELECT u FROM UserProfile u WHERE u.userType = :userType AND u.status = :status")
    List<UserProfile> findActiveLoanOfficers(
        @Param("userType") UserProfile.UserType userType, 
        @Param("status") UserProfile.UserStatus status
    );
    
    /**
     * Find users by name (first or last name containing the search term)
     */
    @Query("SELECT u FROM UserProfile u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserProfile> findByNameContaining(@Param("name") String name);
    
    /**
     * Find users by employee ID
     */
    Optional<UserProfile> findByEmployeeId(String employeeId);
}
