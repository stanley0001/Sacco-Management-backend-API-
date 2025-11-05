package com.example.demo.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for M-PESA PayBill C2B payment request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayBillC2BRequest {
    private String TransactionType;      // e.g., "Pay Bill"
    private String TransID;              // M-PESA Transaction ID (receipt number)
    private String TransTime;            // Transaction timestamp (YYYYMMDDHHmmss)
    private String TransAmount;          // Amount paid
    private String BusinessShortCode;    // PayBill number
    private String BillRefNumber;        // Account number (document number)
    private String InvoiceNumber;        // Optional invoice number
    private String OrgAccountBalance;    // Organization account balance (optional)
    private String ThirdPartyTransID;    // Optional third party transaction ID
    private String MSISDN;               // Customer phone number
    private String FirstName;            // Customer first name
    private String MiddleName;           // Customer middle name
    private String LastName;             // Customer last name
}
