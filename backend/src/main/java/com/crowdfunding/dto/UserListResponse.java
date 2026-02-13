package com.crowdfunding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String status; // ACTIVE, BLOCKED
    private List<String> roles;
    private String walletAddress;
    private LocalDateTime createdAt;
}
