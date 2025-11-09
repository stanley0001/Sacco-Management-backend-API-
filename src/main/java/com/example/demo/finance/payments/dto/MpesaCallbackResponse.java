package com.example.demo.finance.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpesaCallbackResponse {
    
    @JsonProperty("Body")
    private Body body;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StkCallback {
        @JsonProperty("MerchantRequestID")
        private String merchantRequestId;
        
        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestId;
        
        @JsonProperty("ResultCode")
        private Integer resultCode;
        
        @JsonProperty("ResultDesc")
        private String resultDesc;
        
        @JsonProperty("CallbackMetadata")
        private CallbackMetadata callbackMetadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallbackMetadata {
        @JsonProperty("Item")
        private List<Item> item;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @JsonProperty("Name")
        private String name;
        
        @JsonProperty("Value")
        private Object value;
    }
}
