package com.crowdfunding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Donation Entity - Stores transaction references
 * Actual donation amount stored on blockchain
 */
@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;
    
    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;
    
    @Column(name = "transaction_hash", unique = true, nullable = false, length = 66)
    private String transactionHash; // Ethereum transaction hash
    
    @Column(name = "amount", precision = 36, scale = 18)
    private BigDecimal amount; // In ETH (for display, synced from blockchain)
    
    @Column(name = "donated_at")
    private LocalDateTime donatedAt;
    
    @Column(name = "block_number")
    private Long blockNumber;
}
