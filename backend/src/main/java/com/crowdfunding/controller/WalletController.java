package com.crowdfunding.controller;

import com.crowdfunding.dto.WalletConnectRequest;
import com.crowdfunding.dto.WalletResponse;
import com.crowdfunding.entity.Wallet;
import com.crowdfunding.repository.WalletRepository;
import com.crowdfunding.security.UserPrincipal;
import com.crowdfunding.service.UserService;
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
    private final WalletRepository walletRepository;
    
    @PostMapping("/connect")
    public ResponseEntity<?> connectWallet(
            @RequestBody WalletConnectRequest request,
            Authentication authentication) {
        if (request == null || request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Wallet address is required");
        }
        if (!request.getAddress().matches("^0x[a-fA-F0-9]{40}$")) {
            return ResponseEntity.badRequest().body("Invalid Ethereum address format");
        }
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Authentication required");
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserPrincipal)) {
                return ResponseEntity.status(403).body("Invalid authentication token");
            }
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            Wallet wallet = userService.connectWallet(userPrincipal.getId(), request.getAddress());
            return ResponseEntity.ok().body("Wallet connected successfully: " + wallet.getAddress());
        } catch (ClassCastException e) {
            return ResponseEntity.status(403).body("Invalid authentication token");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
    @GetMapping("/my-wallet")
    public ResponseEntity<?> getMyWallet(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Authentication required");
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserPrincipal)) {
                return ResponseEntity.status(403).body("Invalid authentication");
            }
            Wallet wallet = walletRepository.findByUserId(((UserPrincipal) principal).getId()).orElse(null);
            if (wallet == null) {
                return ResponseEntity.ok().body("No wallet connected");
            }
            WalletResponse response = new WalletResponse(
                wallet.getAddress(),
                wallet.getConnectedAt(),
                wallet.getIsVerified()
            );
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching wallet: " + e.getMessage());
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok().body(Map.of("authenticated", false, "message", "Not authenticated"));
        }
        return ResponseEntity.ok().body(Map.of(
            "authenticated", authentication.isAuthenticated(),
            "principalType", authentication.getPrincipal().getClass().getSimpleName()
        ));
    }
}
