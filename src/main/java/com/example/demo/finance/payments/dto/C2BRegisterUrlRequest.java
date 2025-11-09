package com.example.demo.finance.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * C2B Register URL Request DTO
 * Used for registering validation and confirmation URLs with Safaricom Daraja API
 * 
 * IMPORTANT: Uses @JsonProperty to ensure exact field names are sent to M-PESA API
 * M-PESA API is case-sensitive and expects these exact field names
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class C2BRegisterUrlRequest {

    /**
     * Organization shortcode (Paybill or Till Number)
     * Example: 402634, 600986, 174379
     */
    @JsonProperty("ShortCode")
    private String shortCode;

    /**
     * Response type - determines what happens if validation URL is unreachable
     * Values: "Completed" or "Cancelled"
     * - Completed: M-PESA will complete transaction if validation fails
     * - Cancelled: M-PESA will cancel transaction if validation fails
     */
    @JsonProperty("ResponseType")
    private String responseType;

    /**
     * URL that receives payment confirmation after successful transaction
     * Must be HTTPS in production (HTTP allowed in sandbox)
     * Example: https://yourdomain.com/api/auto-pay/callback/confirm
     */
    @JsonProperty("ConfirmationURL")
    private String confirmationURL;

    /**
     * URL that receives validation requests (optional feature, must be enabled by Safaricom)
     * Must be HTTPS in production (HTTP allowed in sandbox)
     * Example: https://yourdomain.com/api/auto-pay/callback/validate
     */
    @JsonProperty("ValidationURL")
    private String validationURL;
}
