package com.crowdfunding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CampaignCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private String imageUrl;
    
    private String category;
    
    @NotNull(message = "Goal amount is required")
    @Positive(message = "Goal amount must be positive")
    private BigDecimal goalAmount; // In ETH
    
    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;
}
