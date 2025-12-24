package com.crowdfunding.repository;

import com.crowdfunding.entity.Campaign;
import com.crowdfunding.entity.Donation;
import com.crowdfunding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByTransactionHash(String transactionHash);
    List<Donation> findByDonor(User donor);
    List<Donation> findByCampaign(Campaign campaign);
    List<Donation> findByCampaignAndDonor(Campaign campaign, User donor);
}
