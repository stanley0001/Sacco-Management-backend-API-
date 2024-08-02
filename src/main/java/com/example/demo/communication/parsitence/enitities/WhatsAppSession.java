package com.example.demo.communication.parsitence.enitities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhatsAppSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true,nullable = false)
    private Long id;
    private String phone;
    private String sessionLog;
    private String previousMenu;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
