package com.crowdfunding.service;

import com.crowdfunding.entity.Role;
import com.crowdfunding.entity.User;
import com.crowdfunding.entity.Wallet;
import com.crowdfunding.repository.RoleRepository;
import com.crowdfunding.repository.UserRepository;
import com.crowdfunding.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public User signup(String username, String email, String password, String fullName,
                       String phoneNumber, String country) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setCountry(country);
        
        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));
        
        return userRepository.save(user);
    }
    
    @Transactional
    public Wallet connectWallet(Long userId, String walletAddress) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if wallet already exists
        if (walletRepository.existsByAddress(walletAddress)) {
            throw new RuntimeException("Wallet address already connected to another user");
        }
        
        // Check if user already has a wallet - update it instead of creating new
        Wallet existingWallet = walletRepository.findByUserId(userId).orElse(null);
        if (existingWallet != null) {
            // Update existing wallet address
            existingWallet.setAddress(walletAddress);
            existingWallet.setConnectedAt(LocalDateTime.now());
            return walletRepository.save(existingWallet);
        }
        
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAddress(walletAddress);
        wallet.setConnectedAt(LocalDateTime.now());
        wallet.setIsVerified(false);
        
        return walletRepository.save(wallet);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
