package com.crowdfunding.service;

import com.crowdfunding.dto.DonationResponse;
import com.crowdfunding.entity.Campaign;
import com.crowdfunding.entity.Donation;
import com.crowdfunding.entity.User;
import com.crowdfunding.repository.CampaignRepository;
import com.crowdfunding.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Donation Service - Stores transaction references
 * Actual donation amounts stored on blockchain
 */
@Service
@RequiredArgsConstructor
public class DonationService {
    
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    
    /**
     * Record donation transaction hash
     * Called after user donates via MetaMask
     */
    @Transactional
    public Donation recordDonation(Long campaignId, User donor, String transactionHash,
                                   BigDecimal amount, Long blockNumber) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // Check if transaction already recorded
        if (donationRepository.findByTransactionHash(transactionHash).isPresent()) {
            throw new RuntimeException("Transaction already recorded");
        }
        
        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setTransactionHash(transactionHash);
        donation.setAmount(amount);
        donation.setDonatedAt(LocalDateTime.now());
        donation.setBlockNumber(blockNumber);
        
        return donationRepository.save(donation);
    }
    
    public List<DonationResponse> getDonationsByDonor(User donor) {
        return donationRepository.findByDonor(donor).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public List<DonationResponse> getDonationsByCampaign(Campaign campaign) {
        return donationRepository.findByCampaign(campaign).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    private DonationResponse toResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setCampaignId(donation.getCampaign().getId());
        response.setCampaignTitle(donation.getCampaign().getTitle());
        response.setTransactionHash(donation.getTransactionHash());
        response.setAmount(donation.getAmount());
        response.setDonatedAt(donation.getDonatedAt());
        if (donation.getDonor().getWallet() != null) {
            response.setDonorWalletAddress(donation.getDonor().getWallet().getAddress());
        }
        return response;
    }
}
