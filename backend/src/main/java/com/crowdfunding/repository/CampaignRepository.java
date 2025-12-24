package com.crowdfunding.repository;

import com.crowdfunding.entity.Campaign;
import com.crowdfunding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByBlockchainId(Long blockchainId);
    List<Campaign> findByCreator(User creator);
    List<Campaign> findByGoalReached(boolean goalReached);
    List<Campaign> findAllByOrderByCreatedAtDesc();
}
