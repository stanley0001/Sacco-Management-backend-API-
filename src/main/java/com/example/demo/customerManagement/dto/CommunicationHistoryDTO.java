package com.example.demo.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationHistoryDTO {
    
    private Long id;
    private String communicationType; // SMS, EMAIL, WHATSAPP
    private String content;
    private String recipient;
    private String status;
    private LocalDateTime sentAt;
    private String sentBy;
}
