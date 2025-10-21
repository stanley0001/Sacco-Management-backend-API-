package com.example.demo.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class STKPushResponse {
    
    @JsonProperty("MerchantRequestID")
    private String merchantRequestId;
    
    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestId;
    
    @JsonProperty("ResponseCode")
    private String responseCode;
    
    @JsonProperty("ResponseDescription")
    private String responseDescription;
    
    @JsonProperty("CustomerMessage")
    private String customerMessage;
}
