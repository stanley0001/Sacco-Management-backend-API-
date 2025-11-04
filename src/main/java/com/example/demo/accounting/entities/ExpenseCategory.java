package com.example.demo.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Expense Category - Classification for expenses
 */
@Entity
@Table(name = "expense_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    private String accountCode; // Chart of Accounts mapping

    private Double budgetAmount; // Monthly/Annual budget

    private Boolean isActive = true;

    private Long parentId; // For subcategories
}
