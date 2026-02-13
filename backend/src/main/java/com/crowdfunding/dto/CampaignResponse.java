package com.crowdfunding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {
    private Long id;
    private Long blockchainId;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private BigDecimal goalAmount;
    private BigDecimal totalRaised;
    private LocalDateTime deadline;
    private Boolean goalReached;
    private Boolean fundsWithdrawn;
    private String creatorUsername;
    private String creatorWalletAddress;
    private String status; // ACTIVE, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private Long donationCount;
}
