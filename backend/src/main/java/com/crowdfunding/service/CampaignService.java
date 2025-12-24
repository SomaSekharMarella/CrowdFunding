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
        
        return campaignRepository.save(campaign);
    }
    
    /**
     * Sync campaign data from blockchain
     */
    @Transactional
    public void syncCampaignFromBlockchain(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        try {
            BlockchainService.CampaignData blockchainData = 
                blockchainService.getCampaign(campaign.getBlockchainId());
            
            // Convert Wei to ETH
            BigDecimal goalEth = new BigDecimal(blockchainData.goal)
                .divide(new BigDecimal(BigInteger.TEN.pow(18)), 18, BigDecimal.ROUND_DOWN);
            BigDecimal raisedEth = new BigDecimal(blockchainData.totalRaised)
                .divide(new BigDecimal(BigInteger.TEN.pow(18)), 18, BigDecimal.ROUND_DOWN);
            
            campaign.setTotalRaised(raisedEth);
            campaign.setGoalReached(blockchainData.goalReached);
            campaign.setFundsWithdrawn(blockchainData.fundsWithdrawn);
            
            campaignRepository.save(campaign);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync from blockchain: " + e.getMessage());
        }
    }
    
    public List<CampaignResponse> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        return toResponse(campaign);
    }
    
    public List<CampaignResponse> getCampaignsByCreator(User creator) {
        return campaignRepository.findByCreator(creator).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
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
