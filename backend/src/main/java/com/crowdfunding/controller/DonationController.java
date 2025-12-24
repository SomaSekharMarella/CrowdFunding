package com.crowdfunding.controller;

import com.crowdfunding.dto.DonationResponse;
import com.crowdfunding.entity.Donation;
import com.crowdfunding.entity.User;
import com.crowdfunding.repository.CampaignRepository;
import com.crowdfunding.repository.UserRepository;
import com.crowdfunding.security.UserPrincipal;
import com.crowdfunding.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DonationController {
    
    private final DonationService donationService;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    
    @PostMapping("/record")
    public ResponseEntity<?> recordDonation(
            @RequestParam Long campaignId,
            @RequestParam String transactionHash,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Long blockNumber,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User donor = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Donation donation = donationService.recordDonation(
                campaignId,
                donor,
                transactionHash,
                amount,
                blockNumber
            );
            
            return ResponseEntity.ok().body("Donation recorded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/my-donations")
    public ResponseEntity<List<DonationResponse>> getMyDonations(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User donor = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<DonationResponse> donations = donationService.getDonationsByDonor(donor);
        return ResponseEntity.ok(donations);
    }
    
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<DonationResponse>> getCampaignDonations(@PathVariable Long campaignId) {
        var campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        List<DonationResponse> donations = donationService.getDonationsByCampaign(campaign);
        return ResponseEntity.ok(donations);
    }
}
