package com.example.demo.communication.services;

import com.example.demo.communication.parsitence.models.WhatsAppMessage;
import org.springframework.stereotype.Service;

@Service
public interface WhatsAppService {
    String processWhatsAppRequest(WhatsAppMessage message);
}
