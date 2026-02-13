package com.crowdfunding.controller;

import com.crowdfunding.dto.CampaignCreateRequest;
import com.crowdfunding.dto.CampaignResponse;
import com.crowdfunding.entity.Campaign;
import com.crowdfunding.entity.User;
import com.crowdfunding.repository.UserRepository;
import com.crowdfunding.security.UserPrincipal;
import com.crowdfunding.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CampaignController {
    
    private final CampaignService campaignService;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns() {
        return ResponseEntity.ok(campaignService.getActiveCampaigns());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        try {
            CampaignResponse campaign = campaignService.getCampaignById(id);
            return ResponseEntity.ok(campaign);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createCampaign(
            @Valid @RequestBody CampaignCreateRequest request,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User creator = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Note: blockchainId should be provided by frontend after creating campaign on blockchain
            // For now, we'll require it in the request or create a separate endpoint
            // This is a simplified version - in production, frontend creates on blockchain first
            
            return ResponseEntity.badRequest().body(
                "Campaign creation: First create campaign on blockchain, then call POST /api/campaigns/metadata"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/metadata")
    public ResponseEntity<?> createCampaignMetadata(
            @RequestParam Long blockchainId,
            @Valid @RequestBody CampaignCreateRequest request,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User creator = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            if (creator.getStatus() == com.crowdfunding.entity.User.UserStatus.BLOCKED) {
                return ResponseEntity.status(403).body("Blocked users cannot create campaigns");
            }
            Campaign campaign = campaignService.createCampaignMetadata(
                creator,
                blockchainId,
                request.getTitle(),
                request.getDescription(),
                request.getImageUrl(),
                request.getCategory(),
                request.getGoalAmount(),
                request.getDeadline()
            );
            
            return ResponseEntity.ok(campaignService.getCampaignById(campaign.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/my-campaigns")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User creator = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<CampaignResponse> campaigns = campaignService.getCampaignsByCreator(creator);
        return ResponseEntity.ok(campaigns);
    }
    
    @PostMapping("/{id}/sync")
    public ResponseEntity<?> syncCampaignFromBlockchain(@PathVariable Long id) {
        try {
            campaignService.syncCampaignFromBlockchain(id);
            return ResponseEntity.ok().body("Campaign synced successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
