package com.example.demo.channels.mobile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private String memberId;
    private String memberNumber;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String idNumber;
    private String status;
}
