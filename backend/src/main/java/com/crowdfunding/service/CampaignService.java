package com.crowdfunding.service;

import com.crowdfunding.dto.CampaignResponse;
import com.crowdfunding.entity.Campaign;
import com.crowdfunding.entity.User;
import com.crowdfunding.repository.CampaignRepository;
import com.crowdfunding.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {
    
    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final BlockchainService blockchainService;
    
    /**
     * Create campaign metadata in database
     * Note: Actual campaign creation on blockchain happens from frontend
     */
    @Transactional
    public Campaign createCampaignMetadata(User creator, Long blockchainId, String title,
                                          String description, String imageUrl, String category,
                                          BigDecimal goalAmount, java.time.LocalDateTime deadline) {
        Campaign campaign = new Campaign();
        campaign.setBlockchainId(blockchainId);
        campaign.setCreator(creator);
        campaign.setTitle(title);
        campaign.setDescription(description);
        campaign.setImageUrl(imageUrl);
        campaign.setCategory(category);
        campaign.setGoalAmount(goalAmount);
        campaign.setDeadline(deadline);
        campaign.setTotalRaised(BigDecimal.ZERO);
        campaign.setGoalReached(false);
        campaign.setFundsWithdrawn(false);
        campaign.setStatus(Campaign.CampaignStatus.ACTIVE);
        return campaignRepository.save(campaign);
    }
    
    /**
     * Sync campaign data from blockchain
     */
    @Transactional
    public void syncCampaignFromBlockchain(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // Skip sync if blockchainId is not set
        if (campaign.getBlockchainId() == null) {
            System.err.println("Warning: Campaign " + campaignId + " has no blockchainId, skipping sync");
            return;
        }
        
        try {
            BlockchainService.CampaignData blockchainData = 
                blockchainService.getCampaign(campaign.getBlockchainId());
            
            // Convert Wei to ETH
            BigDecimal goalEth = new BigDecimal(blockchainData.goal)
                .divide(new BigDecimal(BigInteger.TEN.pow(18)), 18, RoundingMode.DOWN);
            BigDecimal raisedEth = new BigDecimal(blockchainData.totalRaised)
                .divide(new BigDecimal(BigInteger.TEN.pow(18)), 18, RoundingMode.DOWN);
            
            campaign.setTotalRaised(raisedEth);
            campaign.setGoalReached(blockchainData.goalReached);
            campaign.setFundsWithdrawn(blockchainData.fundsWithdrawn);
            if (blockchainData.active != null && !blockchainData.active) {
                campaign.setStatus(Campaign.CampaignStatus.CANCELLED);
            } else if (Boolean.TRUE.equals(blockchainData.fundsWithdrawn)) {
                campaign.setStatus(Campaign.CampaignStatus.COMPLETED);
            } else {
                campaign.setStatus(Campaign.CampaignStatus.ACTIVE);
            }
            campaignRepository.save(campaign);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync from blockchain: " + e.getMessage());
        }
    }
    
    public List<CampaignResponse> getAllCampaigns() {
        // For list views, return cached data (faster)
        // Individual campaign details will auto-sync when fetched
        return campaignRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /** Active campaigns only (for GET /api/campaign/active) */
    public List<CampaignResponse> getActiveCampaigns() {
        // For list views, return cached data (faster)
        return campaignRepository.findByStatus(Campaign.CampaignStatus.ACTIVE).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // Auto-sync from blockchain to ensure fresh data (totalRaised, goalReached, etc.)
        try {
            if (campaign.getBlockchainId() != null) {
                syncCampaignFromBlockchain(id);
                // Reload campaign after sync to get updated data
                campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Campaign not found"));
            } else {
                System.err.println("Warning: Campaign " + id + " has no blockchainId, cannot sync");
            }
        } catch (Exception e) {
            // Log but don't fail - return cached data if sync fails
            System.err.println("Warning: Failed to sync campaign " + id + " from blockchain: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
        }
        
        return toResponse(campaign);
    }
    
    public List<CampaignResponse> getCampaignsByCreator(User creator) {
        // For list views, return cached data (faster)
        return campaignRepository.findByCreator(creator).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /** Admin: all campaigns (active, completed, cancelled) */
    public List<CampaignResponse> getAllCampaignsForAdmin() {
        return campaignRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void cancelCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        campaign.setStatus(Campaign.CampaignStatus.CANCELLED);
        campaignRepository.save(campaign);
    }

    private CampaignResponse toResponse(Campaign campaign) {
        CampaignResponse response = new CampaignResponse();
        response.setId(campaign.getId());
        response.setBlockchainId(campaign.getBlockchainId());
        response.setTitle(campaign.getTitle());
        response.setDescription(campaign.getDescription());
        response.setImageUrl(campaign.getImageUrl());
        response.setCategory(campaign.getCategory());
        response.setGoalAmount(campaign.getGoalAmount());
        response.setTotalRaised(campaign.getTotalRaised());
        response.setDeadline(campaign.getDeadline());
        response.setGoalReached(campaign.getGoalReached());
        response.setFundsWithdrawn(campaign.getFundsWithdrawn());
        response.setCreatorUsername(campaign.getCreator().getUsername());
        if (campaign.getCreator().getWallet() != null) {
            response.setCreatorWalletAddress(campaign.getCreator().getWallet().getAddress());
        }
        response.setStatus(campaign.getStatus() != null ? campaign.getStatus().name() : "ACTIVE");
        response.setCreatedAt(campaign.getCreatedAt());
        
        try {
            Long donationCount = blockchainService.getDonationCount(campaign.getBlockchainId());
            response.setDonationCount(donationCount);
        } catch (Exception e) {
            response.setDonationCount(0L);
        }
        
        return response;
    }
}
