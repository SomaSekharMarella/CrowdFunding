package com.crowdfunding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wallet Entity - Maps user to blockchain wallet address
 * One user = One wallet address
 */
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "address", nullable = false, unique = true, length = 42)
    private String address; // Ethereum address (0x...)
    
    @Column(name = "connected_at")
    private LocalDateTime connectedAt;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
}
