package com.example.demo.system.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.loanManagement.parsistence.entities.*;
import com.example.demo.loanManagement.parsistence.repositories.*;
import com.example.demo.savingsManagement.persistence.entities.SavingsAccount;
import com.example.demo.savingsManagement.persistence.entities.SavingsProduct;
import com.example.demo.savingsManagement.persistence.entities.SavingsTransaction;
import com.example.demo.savingsManagement.persistence.repositories.SavingsAccountRepository;
import com.example.demo.savingsManagement.persistence.repositories.SavingsProductRepository;
import com.example.demo.savingsManagement.persistence.repositories.SavingsTransactionRepository;
import com.example.demo.userManagements.parsitence.enitities.Roles;
import com.example.demo.userManagements.parsitence.enitities.Users;
import com.example.demo.userManagements.parsitence.enitities.rolePermissions;
import com.example.demo.userManagements.parsitence.models.Security;
import com.example.demo.userManagements.parsitence.repositories.permissionsRepo;
import com.example.demo.userManagements.parsitence.repositories.rolesRepo;
import com.example.demo.userManagements.parsitence.repositories.securityRepo;
import com.example.demo.userManagements.parsitence.repositories.userRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final Faker faker = new Faker();
    private final Random random = new Random();

    private final CustomerRepo customerRepo;
    private final userRepo userRepository;
    private final rolesRepo rolesRepository;
    private final permissionsRepo permissionsRepository;
    private final securityRepo securityRepository;
    private final ProductRepo productRepo;
    private final SavingsProductRepository savingsProductRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final ApplicationRepo applicationRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final TransactionsRepo transactionsRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepo customerRepo, userRepo userRepository, rolesRepo rolesRepository,
                      permissionsRepo permissionsRepository, securityRepo securityRepository, ProductRepo productRepo, 
                      SavingsProductRepository savingsProductRepository,
                      SavingsAccountRepository savingsAccountRepository,
                      SavingsTransactionRepository savingsTransactionRepository,
                      ApplicationRepo applicationRepo, LoanAccountRepo loanAccountRepo,
                      TransactionsRepo transactionsRepo, BCryptPasswordEncoder passwordEncoder) {
        this.customerRepo = customerRepo;
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.permissionsRepository = permissionsRepository;
        this.securityRepository = securityRepository;
        this.productRepo = productRepo;
        this.savingsProductRepository = savingsProductRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.applicationRepo = applicationRepo;
        this.loanAccountRepo = loanAccountRepo;
        this.transactionsRepo = transactionsRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // Check if data already exists
            if (customerRepo.count() > 50) {
                logger.info("Data already seeded. Skipping seeder...");
//                this.seedPasswords(userRepository.findAll());
                return;
            }

            logger.info("Starting data seeding...");

            // Seed in order of dependencies
            List<rolePermissions> permissions = seedPermissions();
            List<Roles> roles = seedRoles();
            assignPermissionsToRoles(roles, permissions);
            List<Users> users = seedUsers(roles);
            seedPasswords(users);
            List<Customer> customers = seedCustomers(users);
            List<Products> loanProducts = seedLoanProducts();
            List<SavingsProduct> savingsProducts = seedSavingsProducts();
            List<SavingsAccount> savingsAccounts = seedSavingsAccounts(customers, savingsProducts);
            seedSavingsTransactions(savingsAccounts);
            List<LoanApplication> loanApplications = seedLoanApplications(customers, loanProducts);
            List<LoanAccount> loanAccounts = seedLoanAccounts(loanApplications);
            seedLoanTransactions(loanAccounts);

            logger.info("Data seeding completed successfully!");
            logger.info("Created: {} Customers, {} Users, {} Savings Accounts, {} Loan Accounts",
                    customers.size(), users.size(), savingsAccounts.size(), loanAccounts.size());

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.warn("Data integrity violation during seeding (likely duplicate data). Application will continue. Error: {}", e.getMessage());
        } catch (jakarta.validation.ConstraintViolationException e) {
            logger.warn("Validation error during seeding. Application will continue. Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error during data seeding. Application will continue. Error: {}", e.getMessage());
        }
    }

    private List<rolePermissions> seedPermissions() {
        logger.info("Seeding permissions...");
        List<rolePermissions> permissions = new ArrayList<>();

        String[][] permissionData = {
                // Frontend Route Permissions (from app.module.ts)
                {"ADMIN_ACCESS", "ADMIN_ACCESS", "Full administrative access"},
                {"canViewClientProfile", "VIEW_CLIENT_PROFILE", "View client profile information"},
                {"canViewAdminActions", "VIEW_ADMIN_ACTIONS", "View administrative actions"},
                {"canViewClients", "VIEW_CLIENTS", "View clients list and information"},
                {"canViewUsers", "VIEW_USERS", "View users list and management"},
                {"canViewBps", "VIEW_BPS", "View BPS (Business Process Services)"},
                {"VIEW_CUSTOM_LOAN_BOOK_UPLOAD", "LOAN_BOOK_UPLOAD", "Access loan book upload functionality"},
                {"canViewReports", "VIEW_REPORTS", "View system reports"},
                {"canViewProducts", "VIEW_PRODUCTS", "View loan and savings products"},
                {"canViewCommunication", "VIEW_COMMUNICATION", "View communication module"},
                
                // User Management Permissions
                {"USER_CREATE", "CREATE_USER", "Create new users"},
                {"USER_READ", "READ_USER", "View user information"},
                {"USER_UPDATE", "UPDATE_USER", "Update user information"},
                {"USER_DELETE", "DELETE_USER", "Delete users"},
                {"USER_TOGGLE", "TOGGLE_USER", "Enable/disable user accounts"},
                
                // Customer Management Permissions (from CustomerController)
                {"CUSTOMER_CREATE", "CREATE_CUSTOMER", "Create new customers"},
                {"CUSTOMER_READ", "READ_CUSTOMER", "View customer information"},
                {"CUSTOMER_UPDATE", "UPDATE_CUSTOMER", "Update customer information"},
                {"CUSTOMER_DELETE", "DELETE_CUSTOMER", "Delete customers"},
                {"CUSTOMER_STATUS_CHANGE", "CHANGE_CUSTOMER_STATUS", "Change customer account status"},
                {"CUSTOMER_ENABLE_LOGIN", "ENABLE_CUSTOMER_LOGIN", "Enable customer login access"},
                {"CUSTOMER_SCORE", "VIEW_CUSTOMER_SCORE", "View customer credit score"},
                
                // Loan Management Permissions (from ProductController & LoanBookController)
                {"LOAN_CREATE", "CREATE_LOAN", "Create loan applications"},
                {"LOAN_READ", "READ_LOAN", "View loan information"},
                {"LOAN_UPDATE", "UPDATE_LOAN", "Update loan information"},
                {"LOAN_DELETE", "DELETE_LOAN", "Delete loan records"},
                {"LOAN_APPROVE", "APPROVE_LOAN", "Approve loan applications"},
                {"LOAN_DISBURSE", "DISBURSE_LOAN", "Disburse approved loans"},
                {"LOAN_REPAYMENT", "PROCESS_LOAN_REPAYMENT", "Process loan repayments"},
                {"LOAN_CUSTOM_PAYMENT", "CUSTOM_LOAN_PAYMENT", "Process custom loan payments"},
                {"LOAN_ACCOUNT_VIEW", "VIEW_LOAN_ACCOUNTS", "View loan account details"},
                {"LOAN_BOOK_UPLOAD", "UPLOAD_LOAN_BOOK", "Upload loan book data"},
                
                // Product Management Permissions
                {"PRODUCT_CREATE", "CREATE_PRODUCT", "Create loan and savings products"},
                {"PRODUCT_READ", "READ_PRODUCT", "View product information"},
                {"PRODUCT_UPDATE", "UPDATE_PRODUCT", "Update product information"},
                {"PRODUCT_DELETE", "DELETE_PRODUCT", "Delete products"},
                {"PRODUCT_TOGGLE_STATUS", "TOGGLE_PRODUCT_STATUS", "Enable/disable products"},
                
                // Savings Management Permissions (from SavingsController)
                {"SAVINGS_ACCOUNT_CREATE", "CREATE_SAVINGS_ACCOUNT", "Create savings accounts"},
                {"SAVINGS_ACCOUNT_READ", "READ_SAVINGS_ACCOUNT", "View savings account information"},
                {"SAVINGS_ACCOUNT_UPDATE", "UPDATE_SAVINGS_ACCOUNT", "Update savings account information"},
                {"SAVINGS_ACCOUNT_STATUS", "UPDATE_SAVINGS_STATUS", "Update savings account status"},
                {"SAVINGS_DEPOSIT", "PROCESS_SAVINGS_DEPOSIT", "Process savings deposits"},
                {"SAVINGS_WITHDRAWAL", "PROCESS_SAVINGS_WITHDRAWAL", "Process savings withdrawals"},
                {"SAVINGS_PRODUCT_CREATE", "CREATE_SAVINGS_PRODUCT", "Create savings products"},
                {"SAVINGS_PRODUCT_UPDATE", "UPDATE_SAVINGS_PRODUCT", "Update savings products"},
                {"SAVINGS_STATISTICS", "VIEW_SAVINGS_STATISTICS", "View savings statistics"},
                
                // Transaction Management Permissions
                {"TRANSACTION_CREATE", "CREATE_TRANSACTION", "Process transactions"},
                {"TRANSACTION_READ", "READ_TRANSACTION", "View transaction history"},
                {"TRANSACTION_UPDATE", "UPDATE_TRANSACTION", "Update transaction information"},
                {"TRANSACTION_REVERSE", "REVERSE_TRANSACTION", "Reverse transactions"},
                {"TRANSACTION_APPROVE", "APPROVE_TRANSACTION", "Approve transactions"},
                
                // Subscription Management Permissions
                {"SUBSCRIPTION_CREATE", "CREATE_SUBSCRIPTION", "Create customer subscriptions"},
                {"SUBSCRIPTION_READ", "READ_SUBSCRIPTION", "View subscription information"},
                {"SUBSCRIPTION_UPDATE", "UPDATE_SUBSCRIPTION", "Update subscriptions"},
                {"SUBSCRIPTION_DELETE", "DELETE_SUBSCRIPTION", "Delete subscriptions"},
                
                // Charges Management Permissions
                {"CHARGE_CREATE", "CREATE_CHARGE", "Create product charges"},
                {"CHARGE_READ", "READ_CHARGE", "View charge information"},
                {"CHARGE_UPDATE", "UPDATE_CHARGE", "Update charges"},
                {"CHARGE_DELETE", "DELETE_CHARGE", "Delete charges"},
                
                // Communication Permissions
                {"SMS_SEND", "SEND_SMS", "Send SMS messages"},
                {"WHATSAPP_SEND", "SEND_WHATSAPP", "Send WhatsApp messages"},
                {"BULK_SMS_SEND", "SEND_BULK_SMS", "Send bulk SMS messages"},
                {"COMMUNICATION_VIEW", "VIEW_COMMUNICATION", "View communication history"},
                
                // Reporting Permissions (from ReportsController)
                {"REPORT_VIEW", "VIEW_REPORTS", "View system reports"},
                {"REPORT_GENERATE", "GENERATE_REPORTS", "Generate reports"},
                {"REPORT_EXPORT", "EXPORT_REPORTS", "Export reports"},
                {"DASHBOARD_VIEW", "VIEW_DASHBOARD", "View dashboard data"},
                {"STATISTICS_VIEW", "VIEW_STATISTICS", "View system statistics"},
                
                // System Administration Permissions
                {"SYSTEM_CONFIG", "CONFIGURE_SYSTEM", "Configure system settings"},
                {"ROLE_MANAGE", "MANAGE_ROLES", "Manage user roles and permissions"},
                {"PERMISSION_MANAGE", "MANAGE_PERMISSIONS", "Manage system permissions"},
                {"AUDIT_VIEW", "VIEW_AUDIT_LOGS", "View system audit logs"},
                {"BACKUP_MANAGE", "MANAGE_BACKUPS", "Manage system backups"},
                
                // Payment & Banking Permissions (from MpesaController)
                {"MPESA_PAYMENT", "PROCESS_MPESA_PAYMENT", "Process M-Pesa payments"},
                {"BANK_TRANSFER", "PROCESS_BANK_TRANSFER", "Process bank transfers"},
                {"PAYMENT_GATEWAY", "MANAGE_PAYMENT_GATEWAY", "Manage payment gateway settings"},
                {"SUSPENSE_PAYMENT", "VIEW_SUSPENSE_PAYMENTS", "View suspense payments"},
                
                // Security Permissions
                {"SECURITY_MANAGE", "MANAGE_SECURITY", "Manage security settings"},
                {"PASSWORD_RESET", "RESET_PASSWORDS", "Reset user passwords"},
                {"SESSION_MANAGE", "MANAGE_SESSIONS", "Manage user sessions"},
                {"ACCESS_CONTROL", "MANAGE_ACCESS_CONTROL", "Manage access control settings"},
                
                // Additional Frontend Component Permissions
                {"DASHBOARD_ACCESS", "ACCESS_DASHBOARD", "Access main dashboard"},
                {"PROFILE_VIEW", "VIEW_PROFILE", "View user profile"},
                {"PROFILE_UPDATE", "UPDATE_PROFILE", "Update user profile"},
                {"SETTINGS_VIEW", "VIEW_SETTINGS", "View system settings"},
                {"SETTINGS_UPDATE", "UPDATE_SETTINGS", "Update system settings"},
                
                // Additional Business Logic Permissions
                {"LOAN_DELETE", "DELETE_LOAN", "Delete loan records"},
                {"SAVINGS_DELETE", "DELETE_SAVINGS", "Delete savings records"},
                {"CUSTOMER_DELETE", "DELETE_CUSTOMER", "Delete customer records"},
                {"TRANSACTION_DELETE", "DELETE_TRANSACTION", "Delete transaction records"},
                {"REPORT_DELETE", "DELETE_REPORT", "Delete reports"},
                
                // Advanced Permissions
                {"BULK_OPERATIONS", "PERFORM_BULK_OPERATIONS", "Perform bulk operations"},
                {"DATA_EXPORT", "EXPORT_DATA", "Export system data"},
                {"DATA_IMPORT", "IMPORT_DATA", "Import system data"},
                {"SYSTEM_MAINTENANCE", "PERFORM_MAINTENANCE", "Perform system maintenance"},
                {"USER_IMPERSONATE", "IMPERSONATE_USER", "Impersonate other users"},
                
                // Additional SACCO-specific Permissions
                {"MEMBER_REGISTRATION", "REGISTER_MEMBER", "Register new SACCO members"},
                {"MEMBER_WITHDRAWAL", "WITHDRAW_MEMBER", "Process member withdrawals from SACCO"},
                {"DIVIDEND_CALCULATION", "CALCULATE_DIVIDEND", "Calculate member dividends"},
                {"DIVIDEND_DISTRIBUTION", "DISTRIBUTE_DIVIDEND", "Distribute dividends to members"},
                {"SHARE_MANAGEMENT", "MANAGE_SHARES", "Manage member shares"},
                {"INTEREST_CALCULATION", "CALCULATE_INTEREST", "Calculate loan and savings interest"},
                {"PENALTY_MANAGEMENT", "MANAGE_PENALTIES", "Manage loan penalties and fees"},
                {"COLLATERAL_MANAGEMENT", "MANAGE_COLLATERAL", "Manage loan collateral"},
                {"GUARANTOR_MANAGEMENT", "MANAGE_GUARANTORS", "Manage loan guarantors"},
                {"MEETING_MANAGEMENT", "MANAGE_MEETINGS", "Manage SACCO meetings"},
                {"VOTING_MANAGEMENT", "MANAGE_VOTING", "Manage member voting"},
                {"COMMITTEE_MANAGEMENT", "MANAGE_COMMITTEES", "Manage SACCO committees"},
                {"FINANCIAL_STATEMENTS", "VIEW_FINANCIAL_STATEMENTS", "View financial statements"},
                {"BUDGET_MANAGEMENT", "MANAGE_BUDGET", "Manage SACCO budget"},
                {"ASSET_MANAGEMENT", "MANAGE_ASSETS", "Manage SACCO assets"},
                {"LIABILITY_MANAGEMENT", "MANAGE_LIABILITIES", "Manage SACCO liabilities"},
                {"CASH_MANAGEMENT", "MANAGE_CASH", "Manage cash transactions"},
                {"BANK_RECONCILIATION", "RECONCILE_BANK", "Perform bank reconciliation"},
                {"REGULATORY_REPORTING", "REGULATORY_REPORTS", "Generate regulatory reports"},
                {"COMPLIANCE_MANAGEMENT", "MANAGE_COMPLIANCE", "Manage regulatory compliance"},
                {"RISK_ASSESSMENT", "ASSESS_RISK", "Perform risk assessments"},
                {"CREDIT_SCORING", "CREDIT_SCORING", "Perform credit scoring"},
                {"LOAN_RESTRUCTURING", "RESTRUCTURE_LOAN", "Restructure existing loans"},
                {"WRITE_OFF_MANAGEMENT", "MANAGE_WRITE_OFFS", "Manage loan write-offs"},
                {"PROVISION_MANAGEMENT", "MANAGE_PROVISIONS", "Manage loan provisions"},
                {"INSURANCE_MANAGEMENT", "MANAGE_INSURANCE", "Manage member insurance"},
                {"NOTIFICATION_MANAGEMENT", "MANAGE_NOTIFICATIONS", "Manage system notifications"},
                {"TEMPLATE_MANAGEMENT", "MANAGE_TEMPLATES", "Manage document templates"},
                {"WORKFLOW_MANAGEMENT", "MANAGE_WORKFLOWS", "Manage business workflows"},
                {"APPROVAL_WORKFLOW", "APPROVAL_WORKFLOW", "Manage approval workflows"},
                {"DOCUMENT_MANAGEMENT", "MANAGE_DOCUMENTS", "Manage member documents"},
                {"KYC_MANAGEMENT", "MANAGE_KYC", "Manage Know Your Customer processes"},
                {"AML_COMPLIANCE", "AML_COMPLIANCE", "Anti-Money Laundering compliance"},
                {"FRAUD_DETECTION", "DETECT_FRAUD", "Detect fraudulent activities"},
                {"MOBILE_BANKING", "MOBILE_BANKING", "Access mobile banking features"},
                {"ONLINE_BANKING", "ONLINE_BANKING", "Access online banking features"},
                {"ATM_MANAGEMENT", "MANAGE_ATM", "Manage ATM transactions"},
                {"CARD_MANAGEMENT", "MANAGE_CARDS", "Manage member cards"},
                {"BRANCH_MANAGEMENT", "MANAGE_BRANCHES", "Manage SACCO branches"},
                {"AGENT_MANAGEMENT", "MANAGE_AGENTS", "Manage SACCO agents"},
                {"COMMISSION_MANAGEMENT", "MANAGE_COMMISSIONS", "Manage agent commissions"},
                {"FLOAT_MANAGEMENT", "MANAGE_FLOAT", "Manage cash float"},
                {"TILL_MANAGEMENT", "MANAGE_TILL", "Manage teller till"},
                {"EOD_PROCESSING", "EOD_PROCESSING", "End of day processing"},
                {"MONTH_END_PROCESSING", "MONTH_END_PROCESSING", "Month end processing"},
                {"YEAR_END_PROCESSING", "YEAR_END_PROCESSING", "Year end processing"}
        };

        for (String[] data : permissionData) {
            rolePermissions permission = new rolePermissions();
            permission.setName(data[0]);
            permission.setValue(data[1]);
            permission.setPermissionStatus("ACTIVE");
            permission.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            permission.setAddedBy("admin");
            permission.setIpAddress("127.0.0.1");
            permissions.add(permissionsRepository.save(permission));
        }

        logger.info("Created {} permissions", permissions.size());
        return permissions;
    }

    private List<Roles> seedRoles() {
        logger.info("Seeding roles...");
        List<Roles> roles = new ArrayList<>();

        String[] roleNames = {"ADMIN", "MANAGER", "TELLER", "LOAN_OFFICER", "ACCOUNTANT", "USER"};
        String[] descriptions = {
                "Full system access",
                "Branch management access",
                "Customer service and transactions",
                "Loan processing and approval",
                "Financial reporting and reconciliation",
                "Basic user access"
        };

        for (int i = 0; i < roleNames.length; i++) {
            Roles role = new Roles();
            role.setRoleName(roleNames[i]);
            role.setDescription(descriptions[i]);
            role.setCreatedAt(LocalDate.now().minusDays(random.nextInt(365)).atStartOfDay());
            roles.add(rolesRepository.save(role));
        }

        logger.info("Created {} roles", roles.size());
        return roles;
    }

    private void assignPermissionsToRoles(List<Roles> roles, List<rolePermissions> permissions) {
        logger.info("Assigning permissions to roles...");
        
        // Create a map for quick permission lookup by name
        Map<String, rolePermissions> permissionMap = new HashMap<>();
        for (rolePermissions permission : permissions) {
            permissionMap.put(permission.getName(), permission);
        }
        
        // Define role-permission mappings
        Map<String, List<String>> rolePermissionMap = new HashMap<>();
        
        // ADMIN gets ALL permissions - complete system access
        rolePermissionMap.put("ADMIN", permissions.stream().map(rolePermissions::getName).toList());
        
        // MANAGER gets comprehensive permissions except critical system config
        rolePermissionMap.put("MANAGER", Arrays.asList(
                // Frontend route permissions
                "ADMIN_ACCESS", "canViewClientProfile", "canViewAdminActions", "canViewClients", "canViewUsers", 
                "canViewBps", "canViewReports", "canViewProducts", "canViewCommunication",
                // User management
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_TOGGLE",
                // Customer management
                "CUSTOMER_CREATE", "CUSTOMER_READ", "CUSTOMER_UPDATE", "CUSTOMER_STATUS_CHANGE", 
                "CUSTOMER_ENABLE_LOGIN", "CUSTOMER_SCORE",
                // Loan management
                "LOAN_CREATE", "LOAN_READ", "LOAN_UPDATE", "LOAN_APPROVE", "LOAN_DISBURSE",
                "LOAN_REPAYMENT", "LOAN_CUSTOM_PAYMENT", "LOAN_ACCOUNT_VIEW",
                // Product management
                "PRODUCT_CREATE", "PRODUCT_READ", "PRODUCT_UPDATE", "PRODUCT_TOGGLE_STATUS",
                // Savings management
                "SAVINGS_ACCOUNT_CREATE", "SAVINGS_ACCOUNT_READ", "SAVINGS_ACCOUNT_UPDATE", 
                "SAVINGS_ACCOUNT_STATUS", "SAVINGS_DEPOSIT", "SAVINGS_WITHDRAWAL",
                "SAVINGS_PRODUCT_CREATE", "SAVINGS_PRODUCT_UPDATE", "SAVINGS_STATISTICS",
                // Transactions
                "TRANSACTION_CREATE", "TRANSACTION_READ", "TRANSACTION_UPDATE", "TRANSACTION_APPROVE",
                // Subscriptions & Charges
                "SUBSCRIPTION_CREATE", "SUBSCRIPTION_READ", "SUBSCRIPTION_UPDATE", 
                "CHARGE_CREATE", "CHARGE_READ", "CHARGE_UPDATE",
                // Communication
                "SMS_SEND", "WHATSAPP_SEND", "BULK_SMS_SEND", "COMMUNICATION_VIEW",
                // Reporting
                "REPORT_VIEW", "REPORT_GENERATE", "REPORT_EXPORT", "DASHBOARD_VIEW", "STATISTICS_VIEW",
                // Payments
                "MPESA_PAYMENT", "BANK_TRANSFER", "SUSPENSE_PAYMENT",
                // Role management
                "ROLE_MANAGE",
                // Additional permissions
                "DASHBOARD_ACCESS", "PROFILE_VIEW", "PROFILE_UPDATE", "DATA_EXPORT",
                // SACCO management permissions
                "MEMBER_REGISTRATION", "DIVIDEND_CALCULATION", "DIVIDEND_DISTRIBUTION", 
                "SHARE_MANAGEMENT", "INTEREST_CALCULATION", "PENALTY_MANAGEMENT",
                "FINANCIAL_STATEMENTS", "BUDGET_MANAGEMENT", "REGULATORY_REPORTING",
                "COMPLIANCE_MANAGEMENT", "BRANCH_MANAGEMENT", "AGENT_MANAGEMENT"
        ));
        
        // TELLER gets customer service and transaction permissions
        rolePermissionMap.put("TELLER", Arrays.asList(
                // Frontend access
                "DASHBOARD_ACCESS", "canViewClients", "canViewProducts",
                // Customer operations
                "CUSTOMER_READ", "CUSTOMER_UPDATE", "CUSTOMER_SCORE",
                // Savings operations
                "SAVINGS_ACCOUNT_READ", "SAVINGS_ACCOUNT_UPDATE", "SAVINGS_DEPOSIT", 
                "SAVINGS_WITHDRAWAL", "SAVINGS_STATISTICS",
                // Basic loan operations
                "LOAN_READ", "LOAN_REPAYMENT", "LOAN_ACCOUNT_VIEW",
                // Transaction processing
                "TRANSACTION_CREATE", "TRANSACTION_READ",
                // Subscriptions
                "SUBSCRIPTION_READ", "SUBSCRIPTION_CREATE",
                // Communication
                "SMS_SEND", "COMMUNICATION_VIEW",
                // Payments
                "MPESA_PAYMENT", "BANK_TRANSFER",
                // Basic reporting
                "DASHBOARD_VIEW", "STATISTICS_VIEW",
                // SACCO teller operations
                "CASH_MANAGEMENT", "TILL_MANAGEMENT", "MEMBER_REGISTRATION",
                "KYC_MANAGEMENT", "DOCUMENT_MANAGEMENT"
        ));
        
        // LOAN_OFFICER gets comprehensive loan-related permissions
        rolePermissionMap.put("LOAN_OFFICER", Arrays.asList(
                // Frontend access
                "DASHBOARD_ACCESS", "canViewClients", "canViewProducts", "canViewReports", "VIEW_CUSTOM_LOAN_BOOK_UPLOAD",
                // Customer access
                "CUSTOMER_READ", "CUSTOMER_SCORE",
                // Comprehensive loan management
                "LOAN_CREATE", "LOAN_READ", "LOAN_UPDATE", "LOAN_APPROVE", "LOAN_DISBURSE",
                "LOAN_REPAYMENT", "LOAN_CUSTOM_PAYMENT", "LOAN_ACCOUNT_VIEW", "LOAN_BOOK_UPLOAD",
                // Product management
                "PRODUCT_READ", "PRODUCT_UPDATE",
                // Transaction access
                "TRANSACTION_READ", "TRANSACTION_CREATE",
                // Charges
                "CHARGE_READ", "CHARGE_CREATE",
                // Communication
                "SMS_SEND", "COMMUNICATION_VIEW",
                // Reporting
                "REPORT_VIEW", "REPORT_GENERATE", "DASHBOARD_VIEW", "STATISTICS_VIEW",
                // Payments
                "MPESA_PAYMENT", "SUSPENSE_PAYMENT",
                // SACCO loan officer operations
                "COLLATERAL_MANAGEMENT", "GUARANTOR_MANAGEMENT", "CREDIT_SCORING",
                "LOAN_RESTRUCTURING", "RISK_ASSESSMENT", "KYC_MANAGEMENT"
        ));
        
        // ACCOUNTANT gets financial reporting and read permissions
        rolePermissionMap.put("ACCOUNTANT", Arrays.asList(
                // Frontend access
                "DASHBOARD_ACCESS", "canViewClients", "canViewReports", "canViewProducts",
                // Read-only customer access
                "CUSTOMER_READ",
                // Read-only loan access
                "LOAN_READ", "LOAN_ACCOUNT_VIEW",
                // Read-only savings access
                "SAVINGS_ACCOUNT_READ", "SAVINGS_STATISTICS",
                // Transaction viewing
                "TRANSACTION_READ",
                // Subscription viewing
                "SUBSCRIPTION_READ",
                // Charge viewing
                "CHARGE_READ",
                // Comprehensive reporting
                "REPORT_VIEW", "REPORT_GENERATE", "REPORT_EXPORT", "DASHBOARD_VIEW", "STATISTICS_VIEW",
                // Payment viewing
                "SUSPENSE_PAYMENT",
                // SACCO accounting operations
                "FINANCIAL_STATEMENTS", "BUDGET_MANAGEMENT", "ASSET_MANAGEMENT", 
                "LIABILITY_MANAGEMENT", "BANK_RECONCILIATION", "REGULATORY_REPORTING",
                "COMPLIANCE_MANAGEMENT", "DIVIDEND_CALCULATION", "INTEREST_CALCULATION",
                "EOD_PROCESSING", "MONTH_END_PROCESSING", "YEAR_END_PROCESSING"
        ));
        
        // USER gets basic read-only permissions
        rolePermissionMap.put("USER", Arrays.asList(
                // Limited frontend access
                "DASHBOARD_ACCESS", "canViewClients",
                // Basic read permissions
                "CUSTOMER_READ", "LOAN_READ", "SAVINGS_ACCOUNT_READ", "TRANSACTION_READ",
                "SUBSCRIPTION_READ", "PRODUCT_READ",
                // Basic dashboard access
                "DASHBOARD_VIEW"
        ));
        
        // Actually assign permissions to roles in the database
        for (Roles role : roles) {
            List<String> permissionNames = rolePermissionMap.get(role.getRoleName());
            if (permissionNames != null) {
                List<rolePermissions> rolePermissions = new ArrayList<>();
                
                for (String permissionName : permissionNames) {
                    rolePermissions permission = permissionMap.get(permissionName);
                    if (permission != null) {
                        rolePermissions.add(permission);
                    } else {
                        logger.warn("Permission '{}' not found for role '{}'", permissionName, role.getRoleName());
                    }
                }
                
                // Set the permissions for this role
                role.setPermissions(rolePermissions);
                
                // Save the role with its permissions
                rolesRepository.save(role);
                
                logger.info("Role '{}' assigned {} permissions successfully", 
                    role.getRoleName(), rolePermissions.size());
            }
        }
        
        logger.info("Permission assignment completed - ADMIN has access to ALL {} permissions", permissions.size());
    }

    private List<Users> seedUsers(List<Roles> roles) {
        logger.info("Seeding users...");
        List<Users> users = new ArrayList<>();

        // Create admin user
        Users admin = new Users();
        admin.setFirstName("Admin");
        admin.setLastName("System");
        admin.setOtherName("Super");
        admin.setUserName("admin");
        admin.setEmail("admin@sacco.com");
        admin.setPhone("254700000000");
        admin.setDocumentNumber("00000000");
        admin.setRoleId(roles.get(0).getId().toString());
        admin.setActive(true);
        admin.setCreatedAt(LocalDate.now().minusYears(1));
        admin.setUpdatedAt(LocalDate.now());
        users.add(userRepository.save(admin));

        // Create 20 random users
        for (int i = 0; i < 20; i++) {
            Users user = new Users();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setOtherName(faker.name().firstName());
            user.setUserName(faker.internet().username().toLowerCase() + i);
            user.setEmail(faker.internet().emailAddress());
            user.setPhone("2547" + String.format("%08d", random.nextInt(100000000)));
            user.setDocumentNumber(String.format("%08d", random.nextInt(100000000)));
            user.setRoleId(roles.get(random.nextInt(roles.size())).getId().toString());
            user.setActive(random.nextBoolean() || random.nextBoolean()); // 75% active
            user.setCreatedAt(LocalDate.now().minusDays(random.nextInt(365)));
            user.setUpdatedAt(LocalDate.now().minusDays(random.nextInt(30)));
            users.add(userRepository.save(user));
        }

        logger.info("Created {} users", users.size());
        return users;
    }

    private void seedPasswords(List<Users> users) {
        logger.info("Seeding passwords for users...");
        int count = 0;
        
        for (Users user : users) {
            // Check if password already exists
            if (securityRepository.findByuserId(user.getId().toString()).isPresent()) {
                continue;
            }
            
            Security security = new Security();
            security.setUserId(user.getId().toString());
            
            // Set default password based on username
            String plainPassword;
            if ("admin".equals(user.getUserName())) {
                plainPassword = "Admin@123";
            } else {
                plainPassword = "Password@123";
            }
            
            // Encrypt password
            security.setPassword(passwordEncoder.encode(plainPassword));
            security.setActive(true);
            security.setStatus("ACTIVE");
            security.setStartDate(LocalDate.now());
            security.setEndDate(LocalDate.now().plusYears(1));
            
            securityRepository.save(security);
            count++;
        }
        
        logger.info("Created {} passwords (Admin password: Admin@123, Others: Password@123)", count);
    }

    private List<Customer> seedCustomers(List<Users> users) {
        logger.info("Seeding customers...");
        List<Customer> customers = new ArrayList<>();

        String[] documentTypes = {"ID", "PASSPORT", "ALIEN_ID", "MILITARY_ID"};
        String[] employmentTypes = {"EMPLOYED", "SELF_EMPLOYED", "BUSINESS", "UNEMPLOYED", "RETIRED"};
        String[] maritalStatuses = {"SINGLE", "MARRIED", "DIVORCED", "WIDOWED"};
        String[] occupations = {"Teacher", "Doctor", "Engineer", "Business Owner", "Farmer", "Driver", "Accountant", "Nurse", "Police Officer", "Lawyer"};
        String[] accountStatuses = {"ACTIVE", "DORMANT", "SUSPENDED"};

        // Create 150 customers
        for (int i = 0; i < 150; i++) {
            Customer customer = new Customer();
            customer.setFirstName(faker.name().firstName());
            customer.setMiddleName(faker.name().firstName());
            customer.setLastName(faker.name().lastName());
            customer.setAccountBalance((float) (random.nextInt(500000) + 1000));
            customer.setDocumentType(documentTypes[random.nextInt(documentTypes.length)]);
            customer.setDocumentNumber(String.format("%08d", random.nextInt(100000000)));
            
            // Age between 18 and 70
            customer.setDob(LocalDate.now().minusYears(18 + random.nextInt(52)));
            customer.setEmploymentType(employmentTypes[random.nextInt(employmentTypes.length)]);
            customer.setMaritalStatus(maritalStatuses[random.nextInt(maritalStatuses.length)]);
            customer.setOccupation(occupations[random.nextInt(occupations.length)]);
            customer.setSalary((float) (random.nextInt(150000) + 20000));
            customer.setExternalId("EXT" + String.format("%06d", i + 1));
            customer.setExternalStartDate(LocalDate.now().minusDays(random.nextInt(1825))); // Up to 5 years
            customer.setAddress(faker.address().fullAddress());
            customer.setAccountStatus(accountStatuses[random.nextInt(accountStatuses.length)]);
            customer.setAccountStatusFlag(random.nextBoolean() || random.nextBoolean()); // 75% active
            customer.setStatus(random.nextBoolean() || random.nextBoolean() ? "ACTIVE" : "INACTIVE"); // Mobile banking status
            customer.setNextOfKin(faker.name().fullName());
            customer.setNextOfKinRelationship(random.nextBoolean() ? "SPOUSE" : random.nextBoolean() ? "PARENT" : "SIBLING");
            customer.setNextOfKinDocumentNumber(String.format("%08d", random.nextInt(100000000)));
            customer.setNextOfKinPhone("2547" + String.format("%08d", random.nextInt(100000000)));
            customer.setPhoneNumber("2547" + String.format("%08d", random.nextInt(100000000)));
            customer.setEmail(faker.internet().emailAddress().toLowerCase());
            customer.setAltPhoneNumber("2547" + String.format("%08d", random.nextInt(100000000)));
            customer.setCreatedBy(users.get(random.nextInt(users.size())).getUserName());
            customer.setReferredBy(random.nextBoolean() ? null : "REF" + random.nextInt(1000));
            customer.setBranchCode("BR" + String.format("%03d", random.nextInt(10) + 1));
            customer.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(1825)));
            customer.setUpdatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));

            customers.add(customerRepo.save(customer));
        }

        logger.info("Created {} customers", customers.size());
        return customers;
    }

    private List<Products> seedLoanProducts() {
        logger.info("Seeding loan products...");
        List<Products> products = new ArrayList<>();

        Object[][] productData = {
                {"Emergency Loan", "EMRG001", 30, 5, 50000, 5000, true, false, false, true, "MPESA", "DAYS"},
                {"Personal Loan", "PERS001", 12, 10, 500000, 10000, true, false, false, false, "BANK_TRANSFER", "MONTHS"},
                {"Business Loan", "BUS001", 24, 12, 2000000, 50000, true, true, false, false, "CHEQUE", "MONTHS"},
                {"Asset Finance", "ASSET001", 36, 15, 5000000, 100000, false, false, false, false, "BANK_TRANSFER", "MONTHS"},
                {"Development Loan", "DEV001", 60, 8, 10000000, 500000, false, false, false, false, "BANK_TRANSFER", "MONTHS"},
                {"Education Loan", "EDU001", 48, 6, 1000000, 50000, false, false, false, false, "BANK_TRANSFER", "MONTHS"},
                {"Salary Advance", "SAL001", 1, 3, 100000, 5000, false, false, true, true, "MPESA", "MONTHS"},
                {"Agriculture Loan", "AGRIC001", 18, 9, 3000000, 100000, true, false, false, false, "BANK_TRANSFER", "MONTHS"}
        };

        for (Object[] data : productData) {
            Products product = new Products();
            product.setName((String) data[0]);
            product.setCode((String) data[1]);
            product.setTerm((Integer) data[2]);
            product.setInterest((Integer) data[3]);
            product.setMaxLimit((Integer) data[4]);
            product.setMinLimit((Integer) data[5]);
            product.setTopUp((Boolean) data[6]);
            product.setRollOver((Boolean) data[7]);
            product.setDailyInterest((Boolean) data[8]);
            product.setInterestUpfront((Boolean) data[9]);
            product.setTransactionType((String) data[10]);
            product.setTimeSpan((String) data[11]);
            product.setActive(true);

            products.add(productRepo.save(product));
        }

        logger.info("Created {} loan products", products.size());
        return products;
    }

    private List<SavingsProduct> seedSavingsProducts() {
        logger.info("Seeding savings products...");
        List<SavingsProduct> products = new ArrayList<>();

        Object[][] productData = {
                {"Regular Savings", "SAV001", "Standard savings account", 3.5, 500.0, 100.0, 10.0, 10, 50.0},
                {"Premium Savings", "SAV002", "High interest savings account", 6.0, 5000.0, 5000.0, 5.0, 5, 100.0},
                {"Junior Savings", "SAV003", "Savings account for minors", 4.5, 100.0, 50.0, 20.0, 999, 20.0},
                {"Fixed Deposit", "SAV004", "Fixed term deposit", 8.0, 10000.0, 10000.0, 0.0, 0, 0.0},
                {"Business Savings", "SAV005", "Business savings account", 5.0, 1000.0, 500.0, 15.0, 20, 200.0},
                {"Investment Account", "SAV006", "High yield investment account", 7.5, 50000.0, 50000.0, 0.0, 2, 500.0}
        };

        for (Object[] data : productData) {
            SavingsProduct product = new SavingsProduct();
            product.setName((String) data[0]);
            product.setCode((String) data[1]);
            product.setDescription((String) data[2]);
            product.setInterestRate(BigDecimal.valueOf((Double) data[3]));
            product.setMinimumBalance(BigDecimal.valueOf((Double) data[4]));
            product.setMinimumOpeningBalance(BigDecimal.valueOf((Double) data[5]));
            product.setWithdrawalFee(BigDecimal.valueOf((Double) data[6]));
            product.setMaxWithdrawalsPerMonth((Integer) data[7]);
            product.setMonthlyMaintenanceFee(BigDecimal.valueOf((Double) data[8]));
            product.setInterestCalculationMethod("DAILY_BALANCE");
            product.setInterestPostingFrequency(random.nextBoolean() ? "MONTHLY" : "QUARTERLY");
            product.setMaximumBalance(BigDecimal.valueOf(10000000));
            product.setIsActive(true);
            product.setAllowsWithdrawals(true);
            product.setAllowsDeposits(true);
            product.setAllowsOverdraft(false);
            product.setOverdraftLimit(BigDecimal.ZERO);
            product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            product.setCreatedBy("admin");

            products.add(savingsProductRepository.save(product));
        }

        logger.info("Created {} savings products", products.size());
        return products;
    }

    private List<SavingsAccount> seedSavingsAccounts(List<Customer> customers, List<SavingsProduct> products) {
        logger.info("Seeding savings accounts...");
        List<SavingsAccount> accounts = new ArrayList<>();

        String[] statuses = {"ACTIVE", "ACTIVE", "ACTIVE", "DORMANT", "CLOSED"};
        String[] accountTypes = {"REGULAR", "FIXED_DEPOSIT", "JUNIOR", "BUSINESS"};

        // Create 2-3 accounts per customer (on average 2 per customer = 300 accounts)
        for (Customer customer : customers) {
            int numAccounts = random.nextInt(3) + 1; // 1-3 accounts
            for (int i = 0; i < numAccounts; i++) {
                SavingsProduct product = products.get(random.nextInt(products.size()));
                SavingsAccount account = new SavingsAccount();
                
                account.setCustomerId(customer.getId());
                account.setAccountNumber("SA" + String.format("%010d", accounts.size() + 1));
                account.setProductCode(product.getCode());
                account.setProductName(product.getName());
                
                // Generate realistic balances
                double balance = random.nextInt(500000) + Double.parseDouble(product.getMinimumOpeningBalance().toString());
                account.setBalance(BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP));
                account.setAvailableBalance(account.getBalance().multiply(BigDecimal.valueOf(0.95 + random.nextDouble() * 0.05)));
                
                account.setInterestRate(product.getInterestRate());
                account.setMinimumBalance(product.getMinimumBalance());
                account.setStatus(statuses[random.nextInt(statuses.length)]);
                account.setAccountType(accountTypes[random.nextInt(accountTypes.length)]);
                account.setOpenedDate(LocalDateTime.now().minusDays(random.nextInt(1825)));
                
                if ("CLOSED".equals(account.getStatus())) {
                    account.setClosedDate(LocalDateTime.now().minusDays(random.nextInt(90)));
                }
                
                account.setLastTransactionDate(LocalDateTime.now().minusDays(random.nextInt(60)));
                account.setBranchCode(customer.getBranchCode());
                account.setCreatedBy(customer.getCreatedBy());
                account.setCreatedAt(account.getOpenedDate());
                account.setUpdatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
                account.setNotes(random.nextBoolean() ? null : faker.lorem().sentence());

                accounts.add(savingsAccountRepository.save(account));
            }
        }

        logger.info("Created {} savings accounts", accounts.size());
        return accounts;
    }

    private void seedSavingsTransactions(List<SavingsAccount> accounts) {
        logger.info("Seeding savings transactions...");
        int totalTransactions = 0;
        Set<String> usedTransactionRefs = new HashSet<>();

        String[] transactionTypes = {"DEPOSIT", "DEPOSIT", "DEPOSIT", "WITHDRAWAL", "INTEREST", "FEE"};
        String[] paymentMethods = {"CASH", "MPESA", "BANK_TRANSFER", "CHEQUE"};
        String[] descriptions = {
                "Cash deposit", "Mobile money deposit", "Bank transfer", "Cheque deposit",
                "ATM withdrawal", "Counter withdrawal", "Interest credited", "Monthly maintenance fee"
        };

        // Create 10-30 transactions per active account
        for (SavingsAccount account : accounts) {
            if (!"CLOSED".equals(account.getStatus())) {
                int numTransactions = random.nextInt(21) + 10; // 10-30 transactions
                BigDecimal runningBalance = account.getBalance();

                for (int i = 0; i < numTransactions; i++) {
                    SavingsTransaction transaction = new SavingsTransaction();
                    transaction.setSavingsAccountId(account.getId());
                    
                    // Generate unique transaction reference
                    String transactionRef;
                    do {
                        transactionRef = "TXN" + System.currentTimeMillis() + String.format("%06d", random.nextInt(1000000));
                        try {
                            Thread.sleep(1); // Ensure timestamp changes
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } while (usedTransactionRefs.contains(transactionRef));
                    
                    usedTransactionRefs.add(transactionRef);
                    transaction.setTransactionRef(transactionRef);
                    transaction.setTransactionType(transactionTypes[random.nextInt(transactionTypes.length)]);
                    
                    // Generate realistic amounts
                    BigDecimal amount;
                    if ("INTEREST".equals(transaction.getTransactionType())) {
                        amount = runningBalance.multiply(BigDecimal.valueOf(0.005 + random.nextDouble() * 0.005));
                    } else if ("FEE".equals(transaction.getTransactionType())) {
                        amount = BigDecimal.valueOf(50 + random.nextInt(200));
                    } else if ("DEPOSIT".equals(transaction.getTransactionType())) {
                        amount = BigDecimal.valueOf(1000 + random.nextInt(50000));
                    } else {
                        // For withdrawals, ensure we don't withdraw more than available balance
                        double maxWithdrawal = Math.min(20000, runningBalance.doubleValue() * 0.3);
                        if (maxWithdrawal < 500) {
                            maxWithdrawal = Math.min(500, runningBalance.doubleValue() * 0.5);
                        }
                        amount = BigDecimal.valueOf(Math.max(100, 500 + random.nextInt((int) maxWithdrawal)));
                    }
                    
                    // Ensure amount is always positive and greater than zero
                    amount = amount.max(BigDecimal.ONE);
                    transaction.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
                    transaction.setBalanceBefore(runningBalance);
                    
                    // Update running balance
                    if ("DEPOSIT".equals(transaction.getTransactionType()) || "INTEREST".equals(transaction.getTransactionType())) {
                        runningBalance = runningBalance.add(amount);
                    } else {
                        runningBalance = runningBalance.subtract(amount);
                        // Prevent negative balance
                        if (runningBalance.compareTo(BigDecimal.ZERO) < 0) {
                            runningBalance = BigDecimal.valueOf(100);
                        }
                    }
                    
                    transaction.setBalanceAfter(runningBalance);
                    transaction.setPaymentMethod(paymentMethods[random.nextInt(paymentMethods.length)]);
                    transaction.setPaymentReference("REF" + random.nextInt(100000));
                    transaction.setDescription(descriptions[random.nextInt(descriptions.length)]);
                    
                    LocalDateTime transactionDate = LocalDateTime.now().minusDays(random.nextInt(365));
                    transaction.setTransactionDate(transactionDate);
                    transaction.setValueDate(transactionDate.plusDays(random.nextInt(3)));
                    transaction.setPostedBy("user" + random.nextInt(20));
                    transaction.setApprovedBy(random.nextBoolean() ? "manager" + random.nextInt(5) : null);
                    transaction.setStatus("COMPLETED");
                    transaction.setBranchCode(account.getBranchCode());
                    transaction.setCreatedAt(transactionDate);

                    savingsTransactionRepository.save(transaction);
                    totalTransactions++;
                }
            }
        }

        logger.info("Created {} savings transactions", totalTransactions);
    }

    private List<LoanApplication> seedLoanApplications(List<Customer> customers, List<Products> products) {
        logger.info("Seeding loan applications...");
        List<LoanApplication> applications = new ArrayList<>();
        Set<Long> usedLoanNumbers = new HashSet<>();

        String[] statuses = {"PENDING", "APPROVED", "APPROVED", "APPROVED", "PROCESSED", "REJECTED", "CANCELLED"};
        String[] disbursementTypes = {"MPESA", "BANK_TRANSFER", "CHEQUE", "CASH"};

        // Create 1-2 loan applications per customer
        for (Customer customer : customers) {
            if (random.nextDouble() < 0.7) { // 70% of customers have loans
                int numApplications = random.nextInt(2) + 1;
                for (int i = 0; i < numApplications; i++) {
                    Products product = products.get(random.nextInt(products.size()));
                    LoanApplication application = new LoanApplication();
                    
                    int amount = product.getMinLimit() + random.nextInt(product.getMaxLimit() - product.getMinLimit());
                    
                    // Generate unique loan number
                    long loanNumber;
                    do {
                        loanNumber = System.currentTimeMillis() + random.nextInt(1000000);
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } while (usedLoanNumbers.contains(loanNumber));
                    
                    usedLoanNumbers.add(loanNumber);
                    application.setLoanNumber(loanNumber);
                    application.setCustomerId(customer.getId().toString());
                    application.setCustomerIdNumber(customer.getDocumentNumber());
                    application.setCustomerMobileNumber(customer.getPhoneNumber());
                    application.setLoanAmount(String.valueOf(amount));
                    application.setCreditLimit(String.valueOf(product.getMaxLimit()));
                    application.setDisbursementType(disbursementTypes[random.nextInt(disbursementTypes.length)]);
                    application.setDestinationAccount(customer.getPhoneNumber());
                    application.setApplicationStatus(statuses[random.nextInt(statuses.length)]);
                    application.setProductCode(product.getCode());
                    application.setLoanTerm(String.valueOf(product.getTerm()));
                    application.setLoanInterest(String.valueOf(product.getInterest()));
                    application.setInstallments(String.valueOf(product.getTerm()));
                    application.setApplicationTime(LocalDateTime.now().minusDays(random.nextInt(365)));

                    applications.add(applicationRepo.save(application));
                }
            }
        }

        logger.info("Created {} loan applications", applications.size());
        return applications;
    }

    private List<LoanAccount> seedLoanAccounts(List<LoanApplication> applications) {
        logger.info("Seeding loan accounts...");
        List<LoanAccount> accounts = new ArrayList<>();

        String[] statuses = {"ACTIVE", "ACTIVE", "CLOSED", "DEFAULTED", "RESTRUCTURED"};

        // Create accounts only for approved/processed applications
        for (LoanApplication application : applications) {
            if ("APPROVED".equals(application.getApplicationStatus()) || 
                "PROCESSED".equals(application.getApplicationStatus())) {
                LoanAccount account = new LoanAccount();
                
                account.setApplicationId(application.getApplicationId());
                account.setOtherRef("LN" + String.format("%010d", accounts.size() + 1));
                account.setAmount(Float.parseFloat(application.getLoanAmount()));
                
                float interest = account.getAmount() * (Float.parseFloat(application.getLoanInterest()) / 100);
                account.setPayableAmount(account.getAmount() + interest);
                
                // Set balance based on status
                String status = statuses[random.nextInt(statuses.length)];
                float balance;
                if ("CLOSED".equals(status)) {
                    balance = 0f;
                } else if ("DEFAULTED".equals(status)) {
                    balance = account.getPayableAmount() * (0.6f + random.nextFloat() * 0.4f);
                } else {
                    balance = account.getPayableAmount() * (0.2f + random.nextFloat() * 0.7f);
                }
                
                account.setAccountBalance(balance);
                account.setAmountPaid(account.getPayableAmount() - balance);
                account.setStartDate(application.getApplicationTime().plusDays(random.nextInt(7)));
                account.setDueDate(account.getStartDate().plusMonths(Integer.parseInt(application.getInstallments())));
                account.setStatus(status);
                account.setCustomerId(application.getCustomerId());
                account.setLoanref(account.getOtherRef());
                account.setInstallments(Integer.parseInt(application.getInstallments()));

                accounts.add(loanAccountRepo.save(account));
            }
        }

        logger.info("Created {} loan accounts", accounts.size());
        return accounts;
    }

    private void seedLoanTransactions(List<LoanAccount> accounts) {
        logger.info("Seeding loan transactions...");
        int totalTransactions = 0;

        String[] transactionTypes = {"REPAYMENT", "DISBURSEMENT", "PENALTY", "FEE", "REVERSAL"};
        String[] paymentMethods = {"CASH", "MPESA", "BANK_TRANSFER", "CHEQUE", "STANDING_ORDER"};

        for (LoanAccount account : accounts) {
            // Create disbursement
            com.example.demo.loanManagement.parsistence.entities.loanTransactions disbursement = 
                new com.example.demo.loanManagement.parsistence.entities.loanTransactions();
            disbursement.setAccountId(account.getAccountId());
            disbursement.setCustomerId(account.getCustomerId());
            disbursement.setAmount(account.getAmount());
            disbursement.setTransactionType("DISBURSEMENT");
            disbursement.setPaymentMode("BANK_TRANSFER");
            disbursement.setTransactionDate(account.getStartDate());
            disbursement.setPostedBy("system");
            transactionsRepo.save(disbursement);
            totalTransactions++;

            // Create repayments for non-defaulted accounts
            if (!"DEFAULTED".equals(account.getStatus()) && account.getAmountPaid() != null && account.getAmountPaid() > 0) {
                int numPayments = random.nextInt(15) + 5; // 5-20 payments
                float totalPaid = 0;

                for (int i = 0; i < numPayments && totalPaid < account.getAmountPaid(); i++) {
                    com.example.demo.loanManagement.parsistence.entities.loanTransactions payment = 
                        new com.example.demo.loanManagement.parsistence.entities.loanTransactions();
                    
                    float paymentAmount = Math.min(
                        account.getAmountPaid() - totalPaid,
                        account.getPayableAmount() / account.getInstallments() * (0.8f + random.nextFloat() * 0.4f)
                    );
                    
                    payment.setAccountId(account.getAccountId());
                    payment.setCustomerId(account.getCustomerId());
                    payment.setAmount(paymentAmount);
                    payment.setTransactionType(transactionTypes[random.nextInt(transactionTypes.length)]);
                    payment.setPaymentMode(paymentMethods[random.nextInt(paymentMethods.length)]);
                    payment.setTransactionDate(account.getStartDate().plusDays(random.nextInt(
                        (int) java.time.temporal.ChronoUnit.DAYS.between(account.getStartDate(), LocalDateTime.now()))));
                    payment.setPostedBy("teller" + random.nextInt(10));

                    transactionsRepo.save(payment);
                    totalPaid += paymentAmount;
                    totalTransactions++;
                }
            }
        }

        logger.info("Created {} loan transactions", totalTransactions);
    }
}
