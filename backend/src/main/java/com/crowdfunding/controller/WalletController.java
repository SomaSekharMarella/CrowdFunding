package com.crowdfunding.controller;

import com.crowdfunding.dto.WalletResponse;
import com.crowdfunding.dto.WalletConnectRequest;
import com.crowdfunding.entity.User;
import com.crowdfunding.entity.Wallet;
import com.crowdfunding.repository.UserRepository;
import com.crowdfunding.repository.WalletRepository;
import com.crowdfunding.security.UserPrincipal;
import com.crowdfunding.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class WalletController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    
    @PostMapping("/connect")
    public ResponseEntity<?> connectWallet(
            @RequestBody WalletConnectRequest request,
            Authentication authentication) {
        System.out.println("=== WALLET CONNECT REQUEST ===");
        System.out.println("Request received - Address: " + (request != null ? request.getAddress() : "NULL"));
        System.out.println("Authentication object: " + (authentication != null ? "EXISTS" : "NULL"));
        
        // Manual validation
        if (request == null || request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Wallet address is required");
        }
        
        if (!request.getAddress().matches("^0x[a-fA-F0-9]{40}$")) {
            return ResponseEntity.badRequest().body("Invalid Ethereum address format");
        }
        
        try {
            if (authentication == null) {
                System.out.println("ERROR: Authentication is NULL");
                return ResponseEntity.status(401).body("Authentication required - No authentication object");
            }
            
            System.out.println("Authentication authenticated: " + authentication.isAuthenticated());
            System.out.println("Authentication principal type: " + authentication.getPrincipal().getClass().getName());
            System.out.println("Authentication principal: " + authentication.getPrincipal());
            
            if (!authentication.isAuthenticated()) {
                System.out.println("ERROR: Authentication not authenticated");
                return ResponseEntity.status(401).body("Authentication required - Not authenticated");
            }
            
            Object principal = authentication.getPrincipal();
            System.out.println("Principal class: " + (principal != null ? principal.getClass().getName() : "null"));
            
            if (!(principal instanceof UserPrincipal)) {
                System.out.println("ERROR: Principal is not UserPrincipal, it's: " + 
                    (principal != null ? principal.getClass().getName() : "null"));
                return ResponseEntity.status(403).body("Invalid authentication token - Principal type: " + 
                    (principal != null ? principal.getClass().getName() : "null"));
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            Long userId = userPrincipal.getId();
            System.out.println("User ID: " + userId);
            System.out.println("Wallet address to connect: " + request.getAddress());
            
            Wallet wallet = userService.connectWallet(userId, request.getAddress());
            System.out.println("SUCCESS: Wallet connected - " + wallet.getAddress());
            
            return ResponseEntity.ok().body("Wallet connected successfully: " + wallet.getAddress());
        } catch (ClassCastException e) {
            System.out.println("ERROR: ClassCastException - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(403).body("Invalid authentication token: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("ERROR: RuntimeException - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: Exception - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
    
    @GetMapping("/my-wallet")
    public ResponseEntity<?> getMyWallet(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body("Authentication required");
            }
            
            if (!authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Not authenticated");
            }
            
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserPrincipal)) {
                return ResponseEntity.status(403).body("Invalid authentication - Principal type: " + 
                    (principal != null ? principal.getClass().getName() : "null"));
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            Long userId = userPrincipal.getId();
            
            Wallet wallet = walletRepository.findByUserId(userId)
                .orElse(null);
            
            if (wallet == null) {
                return ResponseEntity.ok().body("No wallet connected");
            }

            return ResponseEntity.ok(new WalletResponse(
                wallet.getAddress(),
                wallet.getConnectedAt(),
                wallet.getIsVerified()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching wallet: " + e.getMessage());
        }
    }
    
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(Authentication authentication) {
        System.out.println("=== TEST AUTH REQUEST ===");
        System.out.println("Authentication object: " + (authentication != null ? "EXISTS" : "NULL"));
        
        if (authentication == null) {
            System.out.println("Authentication is NULL");
            return ResponseEntity.ok().body(Map.of(
                "authenticated", false,
                "message", "Authentication is NULL"
            ));
        }
        
        System.out.println("Authentication authenticated: " + authentication.isAuthenticated());
        System.out.println("Authentication principal type: " + authentication.getPrincipal().getClass().getName());
        
        Map<String, Object> result = Map.of(
            "authenticated", authentication.isAuthenticated(),
            "principalType", authentication.getPrincipal().getClass().getName(),
            "principal", authentication.getPrincipal().toString(),
            "authorities", authentication.getAuthorities().toString()
        );
        
        System.out.println("Test auth result: " + result);
        return ResponseEntity.ok().body(result);
    }
}
