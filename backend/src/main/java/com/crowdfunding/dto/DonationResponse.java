package com.crowdfunding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {
    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private String transactionHash;
    private BigDecimal amount;
    private LocalDateTime donatedAt;
    private String donorWalletAddress;
}
