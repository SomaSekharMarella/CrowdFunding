// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title Crowdfunding
 * @dev Hybrid Blockchain-Based Crowdfunding Platform - Smart Contract
 * @notice Handles ONLY financial operations: donations, withdrawals, refunds
 * @notice Campaign metadata (title, description, images) stored off-chain in MySQL
 */
contract Crowdfunding {
    // ============ STRUCTS ============
    
    /**
     * @dev Campaign structure - stores ONLY financial & trust-critical data
     */
    struct Campaign {
        address owner;           // Campaign creator wallet address
        uint256 goal;            // Funding goal in Wei
        uint256 deadline;        // Unix timestamp
        uint256 totalRaised;    // Total ETH raised in Wei
        bool goalReached;       // Whether goal has been met
        bool fundsWithdrawn;    // Whether creator has withdrawn funds
        bool exists;            // Whether campaign exists
    }
    
    /**
     * @dev Donation structure - tracks individual contributions
     */
    struct Donation {
        address donor;          // Donor wallet address
        uint256 amount;         // Donation amount in Wei
        uint256 timestamp;      // When donation was made
    }
    
    // ============ STATE VARIABLES ============
    
    uint256 public campaignCount;  // Total number of campaigns created
    
    // campaignId => Campaign
    mapping(uint256 => Campaign) public campaigns;
    
    // campaignId => Donation[]
    mapping(uint256 => Donation[]) public donations;
    
    // campaignId => donor address => amount donated
    mapping(uint256 => mapping(address => uint256)) public donorContributions;
    
    // ============ EVENTS ============
    
    event CampaignCreated(
        uint256 indexed campaignId,
        address indexed owner,
        uint256 goal,
        uint256 deadline
    );
    
    event DonationReceived(
        uint256 indexed campaignId,
        address indexed donor,
        uint256 amount,
        uint256 timestamp
    );
    
    event FundsWithdrawn(
        uint256 indexed campaignId,
        address indexed owner,
        uint256 amount
    );
    
    event RefundIssued(
        uint256 indexed campaignId,
        address indexed donor,
        uint256 amount
    );
    
    // ============ MODIFIERS ============
    
    modifier onlyCampaignOwner(uint256 _campaignId) {
        require(campaigns[_campaignId].exists, "Campaign does not exist");
        require(campaigns[_campaignId].owner == msg.sender, "Not campaign owner");
        _;
    }
    
    modifier campaignExists(uint256 _campaignId) {
        require(campaigns[_campaignId].exists, "Campaign does not exist");
        _;
    }
    
    modifier campaignActive(uint256 _campaignId) {
        require(campaigns[_campaignId].exists, "Campaign does not exist");
        require(block.timestamp < campaigns[_campaignId].deadline, "Campaign deadline passed");
        _;
    }
    
    // ============ FUNCTIONS ============
    
    /**
     * @dev Create a new crowdfunding campaign
     * @param _goal Funding goal in Wei
     * @param _deadline Unix timestamp for campaign deadline
     * @return campaignId The ID of the newly created campaign
     */
    function createCampaign(uint256 _goal, uint256 _deadline) external returns (uint256) {
        require(_goal > 0, "Goal must be greater than 0");
        require(_deadline > block.timestamp, "Deadline must be in the future");
        
        campaignCount++;
        uint256 campaignId = campaignCount;
        
        campaigns[campaignId] = Campaign({
            owner: msg.sender,
            goal: _goal,
            deadline: _deadline,
            totalRaised: 0,
            goalReached: false,
            fundsWithdrawn: false,
            exists: true
        });
        
        emit CampaignCreated(campaignId, msg.sender, _goal, _deadline);
        
        return campaignId;
    }
    
    /**
     * @dev Contribute ETH to a campaign
     * @param _campaignId The ID of the campaign to donate to
     */
    function contribute(uint256 _campaignId) external payable campaignActive(_campaignId) {
        require(msg.value > 0, "Donation amount must be greater than 0");
        
        Campaign storage campaign = campaigns[_campaignId];
        
        campaign.totalRaised += msg.value;
        donorContributions[_campaignId][msg.sender] += msg.value;
        
        donations[_campaignId].push(Donation({
            donor: msg.sender,
            amount: msg.value,
            timestamp: block.timestamp
        }));
        
        // Check if goal is reached
        if (campaign.totalRaised >= campaign.goal) {
            campaign.goalReached = true;
        }
        
        emit DonationReceived(_campaignId, msg.sender, msg.value, block.timestamp);
    }
    
    /**
     * @dev Campaign owner withdraws funds if goal is met
     * @param _campaignId The ID of the campaign
     */
    function withdrawFunds(uint256 _campaignId) external onlyCampaignOwner(_campaignId) {
        Campaign storage campaign = campaigns[_campaignId];
        
        require(campaign.goalReached, "Goal not reached");
        require(!campaign.fundsWithdrawn, "Funds already withdrawn");
        require(block.timestamp >= campaign.deadline || campaign.totalRaised >= campaign.goal, 
                "Cannot withdraw before deadline unless goal reached");
        
        campaign.fundsWithdrawn = true;
        
        uint256 amount = campaign.totalRaised;
        
        (bool success, ) = payable(campaign.owner).call{value: amount}("");
        require(success, "Transfer failed");
        
        emit FundsWithdrawn(_campaignId, campaign.owner, amount);
    }
    
    /**
     * @dev Donor claims refund if campaign failed (deadline passed and goal not met)
     * @param _campaignId The ID of the campaign
     */
    function refund(uint256 _campaignId) external campaignExists(_campaignId) {
        Campaign storage campaign = campaigns[_campaignId];
        
        require(block.timestamp >= campaign.deadline, "Campaign deadline not reached");
        require(!campaign.goalReached, "Campaign goal was reached");
        require(!campaign.fundsWithdrawn, "Funds already withdrawn");
        
        uint256 amount = donorContributions[_campaignId][msg.sender];
        require(amount > 0, "No contribution to refund");
        
        // Reset donor contribution to prevent double refund
        donorContributions[_campaignId][msg.sender] = 0;
        
        (bool success, ) = payable(msg.sender).call{value: amount}("");
        require(success, "Refund transfer failed");
        
        emit RefundIssued(_campaignId, msg.sender, amount);
    }
    
    // ============ GETTER FUNCTIONS ============
    
    /**
     * @dev Get campaign details
     * @param _campaignId The ID of the campaign
     * @return owner Campaign owner address
     * @return goal Funding goal in Wei
     * @return deadline Campaign deadline timestamp
     * @return totalRaised Total ETH raised in Wei
     * @return goalReached Whether goal has been met
     * @return fundsWithdrawn Whether funds have been withdrawn
     */
    function getCampaign(uint256 _campaignId) external view returns (
        address owner,
        uint256 goal,
        uint256 deadline,
        uint256 totalRaised,
        bool goalReached,
        bool fundsWithdrawn
    ) {
        Campaign memory campaign = campaigns[_campaignId];
        require(campaign.exists, "Campaign does not exist");
        
        return (
            campaign.owner,
            campaign.goal,
            campaign.deadline,
            campaign.totalRaised,
            campaign.goalReached,
            campaign.fundsWithdrawn
        );
    }
    
    /**
     * @dev Get total number of campaigns
     * @return Total campaign count
     */
    function getCampaignCount() external view returns (uint256) {
        return campaignCount;
    }
    
    /**
     * @dev Get donation count for a campaign
     * @param _campaignId The ID of the campaign
     * @return Number of donations
     */
    function getDonationCount(uint256 _campaignId) external view returns (uint256) {
        return donations[_campaignId].length;
    }
    
    /**
     * @dev Get donation details by index
     * @param _campaignId The ID of the campaign
     * @param _index The index of the donation
     * @return donor Donor address
     * @return amount Donation amount in Wei
     * @return timestamp When donation was made
     */
    function getDonation(uint256 _campaignId, uint256 _index) external view returns (
        address donor,
        uint256 amount,
        uint256 timestamp
    ) {
        require(_index < donations[_campaignId].length, "Invalid donation index");
        
        Donation memory donation = donations[_campaignId][_index];
        return (donation.donor, donation.amount, donation.timestamp);
    }
    
    /**
     * @dev Get total contribution of a donor to a campaign
     * @param _campaignId The ID of the campaign
     * @param _donor The donor address
     * @return Total contribution amount in Wei
     */
    function getDonorContribution(uint256 _campaignId, address _donor) external view returns (uint256) {
        return donorContributions[_campaignId][_donor];
    }
    
    /**
     * @dev Check if campaign is eligible for refund
     * @param _campaignId The ID of the campaign
     * @return True if deadline passed and goal not met
     */
    function isRefundable(uint256 _campaignId) external view returns (bool) {
        Campaign memory campaign = campaigns[_campaignId];
        if (!campaign.exists) return false;
        
        return block.timestamp >= campaign.deadline && 
               !campaign.goalReached && 
               !campaign.fundsWithdrawn;
    }
}
