package com.crowdfunding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Campaign Entity - UI metadata + status (ACTIVE/COMPLETED/CANCELLED)
 * Financial data synced from blockchain
 */
@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    
    public enum CampaignStatus { ACTIVE, COMPLETED, CANCELLED }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "blockchain_id", unique = true, nullable = false)
    private Long blockchainId;
    
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "goal_amount", precision = 36, scale = 18)
    private BigDecimal goalAmount;
    
    @Column(name = "total_raised", precision = 36, scale = 18)
    private BigDecimal totalRaised;
    
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    @Column(name = "goal_reached")
    private Boolean goalReached = false;
    
    @Column(name = "funds_withdrawn")
    private Boolean fundsWithdrawn = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private CampaignStatus status = CampaignStatus.ACTIVE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
