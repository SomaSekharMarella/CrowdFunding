package com.crowdfunding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Entity - User roles (USER, CREATOR, ADMIN)
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 20)
    private RoleName name;
    
    public enum RoleName {
        ROLE_USER,      // Regular donor
        ROLE_CREATOR,   // Campaign creator (has USER role too)
        ROLE_ADMIN      // Admin (off-chain only)
    }
}
