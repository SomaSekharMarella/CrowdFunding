package com.crowdfunding.service;

import com.crowdfunding.dto.CampaignResponse;
import com.crowdfunding.entity.Campaign;
import com.crowdfunding.entity.Donation;
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
     * Sync campaign data.
     *
     * For UI consistency we now treat the database donations as the single
     * source of truth for `totalRaised` that is shown in the frontend.
     *
     * - `totalRaised` = sum of all donations stored in MySQL for this campaign
     * - Blockchain is still queried to update status flags (goalReached,
     *   fundsWithdrawn, active/cancelled), but its `totalRaised` field is
     *   NOT used for display anymore.
     */
    @Transactional
    public void syncCampaignFromBlockchain(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // 1) Always compute totalRaised from DB donations
        BigDecimal totalFromDonations = donationRepository.findByCampaign(campaign)
            .stream()
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2) Best‑effort: update status flags from blockchain (no effect on totalRaised)
        try {
            BlockchainService.CampaignData blockchainData =
                blockchainService.getCampaign(campaign.getBlockchainId());

            campaign.setGoalReached(blockchainData.goalReached);
            campaign.setFundsWithdrawn(blockchainData.fundsWithdrawn);
            if (blockchainData.active != null && !blockchainData.active) {
                campaign.setStatus(Campaign.CampaignStatus.CANCELLED);
            } else if (Boolean.TRUE.equals(blockchainData.fundsWithdrawn)) {
                campaign.setStatus(Campaign.CampaignStatus.COMPLETED);
            } else {
                campaign.setStatus(Campaign.CampaignStatus.ACTIVE);
            }
        } catch (Exception e) {
            // If blockchain is unreachable, just keep existing status; UI totalRaised still correct.
            System.err.println("Warning: could not refresh blockchain status for campaign "
                + campaignId + ": " + e.getMessage());
        }

        // 3) Persist DB‑based totalRaised and derived goalReached flag
        campaign.setTotalRaised(totalFromDonations);
        if (campaign.getGoalAmount() != null
            && totalFromDonations.compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setGoalReached(true);
        }

        campaignRepository.save(campaign);
        System.out.println("Campaign " + campaignId + " synced from DB donations. TotalRaised: " + totalFromDonations);
    }
    
    public List<CampaignResponse> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /** Active campaigns only (for GET /api/campaign/active) */
    public List<CampaignResponse> getActiveCampaigns() {
        return campaignRepository.findByStatus(Campaign.CampaignStatus.ACTIVE).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // Auto-sync from blockchain to ensure latest data (totalRaised, goalReached, etc.)
        // syncCampaignFromBlockchain has built-in retry and fallback to DB calculation
        try {
            syncCampaignFromBlockchain(id);
            // Refresh campaign after sync to get updated values
            campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        } catch (Exception e) {
            // Even if sync fails, try to update from DB donations as last resort
            System.err.println("Warning: Failed to sync campaign " + id + " (blockchainId: " + 
                campaign.getBlockchainId() + ") from blockchain: " + e.getMessage());
            try {
                // Last resort: calculate from DB donations
                BigDecimal totalFromDonations = donationRepository.findByCampaign(campaign)
                    .stream()
                    .map(Donation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalFromDonations.compareTo(BigDecimal.ZERO) > 0) {
                    campaign.setTotalRaised(totalFromDonations);
                    if (campaign.getGoalAmount() != null && totalFromDonations.compareTo(campaign.getGoalAmount()) >= 0) {
                        campaign.setGoalReached(true);
                    }
                    campaignRepository.save(campaign);
                    System.out.println("Updated campaign " + id + " from DB donations as fallback. TotalRaised: " + totalFromDonations);
                }
            } catch (Exception fallbackError) {
                System.err.println("Fallback calculation also failed: " + fallbackError.getMessage());
            }
            e.printStackTrace();
        }
        
        return toResponse(campaign);
    }
    
    public List<CampaignResponse> getCampaignsByCreator(User creator) {
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
        response.setTotalRaised(campaign.getTotalRaised() != null ? campaign.getTotalRaised() : BigDecimal.ZERO);
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
