const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("Crowdfunding Contract", function () {
  let crowdfunding;
  let owner, creator, donor1, donor2;
  
  beforeEach(async function () {
    [owner, creator, donor1, donor2] = await ethers.getSigners();
    
    const Crowdfunding = await ethers.getContractFactory("Crowdfunding");
    crowdfunding = await Crowdfunding.deploy();
    await crowdfunding.waitForDeployment();
  });
  
  describe("Campaign Creation", function () {
    it("Should create a campaign with valid parameters", async function () {
      const goal = ethers.parseEther("10");
      const deadline = Math.floor(Date.now() / 1000) + 86400; // 1 day from now
      
      await expect(crowdfunding.connect(creator).createCampaign(goal, deadline))
        .to.emit(crowdfunding, "CampaignCreated")
        .withArgs(1, creator.address, goal, deadline);
      
      const campaign = await crowdfunding.getCampaign(1);
      expect(campaign.owner).to.equal(creator.address);
      expect(campaign.goal).to.equal(goal);
    });
    
    it("Should reject campaign with zero goal", async function () {
      const deadline = Math.floor(Date.now() / 1000) + 86400;
      await expect(
        crowdfunding.connect(creator).createCampaign(0, deadline)
      ).to.be.revertedWith("Goal must be greater than 0");
    });
    
    it("Should reject campaign with past deadline", async function () {
      const goal = ethers.parseEther("10");
      const deadline = Math.floor(Date.now() / 1000) - 86400; // 1 day ago
      await expect(
        crowdfunding.connect(creator).createCampaign(goal, deadline)
      ).to.be.revertedWith("Deadline must be in the future");
    });
  });
  
  describe("Donations", function () {
    let campaignId;
    const goal = ethers.parseEther("10");
    
    beforeEach(async function () {
      const deadline = Math.floor(Date.now() / 1000) + 86400;
      const tx = await crowdfunding.connect(creator).createCampaign(goal, deadline);
      const receipt = await tx.wait();
      campaignId = 1;
    });
    
    it("Should accept donations", async function () {
      const donationAmount = ethers.parseEther("1");
      
      await expect(
        crowdfunding.connect(donor1).contribute(campaignId, { value: donationAmount })
      ).to.emit(crowdfunding, "DonationReceived");
      
      const campaign = await crowdfunding.getCampaign(campaignId);
      expect(campaign.totalRaised).to.equal(donationAmount);
    });
    
    it("Should reject zero donations", async function () {
      await expect(
        crowdfunding.connect(donor1).contribute(campaignId, { value: 0 })
      ).to.be.revertedWith("Donation amount must be greater than 0");
    });
    
    it("Should mark goal as reached when total raised >= goal", async function () {
      await crowdfunding.connect(donor1).contribute(campaignId, { value: goal });
      
      const campaign = await crowdfunding.getCampaign(campaignId);
      expect(campaign.goalReached).to.be.true;
    });
  });
  
  describe("Withdrawal", function () {
    let campaignId;
    const goal = ethers.parseEther("10");
    
    beforeEach(async function () {
      const deadline = Math.floor(Date.now() / 1000) + 86400;
      await crowdfunding.connect(creator).createCampaign(goal, deadline);
      campaignId = 1;
    });
    
    it("Should allow withdrawal when goal is reached", async function () {
      await crowdfunding.connect(donor1).contribute(campaignId, { value: goal });
      
      const initialBalance = await ethers.provider.getBalance(creator.address);
      const tx = await crowdfunding.connect(creator).withdrawFunds(campaignId);
      const receipt = await tx.wait();
      const finalBalance = await ethers.provider.getBalance(creator.address);
      
      expect(finalBalance - initialBalance).to.be.closeTo(goal, ethers.parseEther("0.01"));
    });
    
    it("Should reject withdrawal if goal not reached", async function () {
      await crowdfunding.connect(donor1).contribute(campaignId, { value: ethers.parseEther("5") });
      
      await expect(
        crowdfunding.connect(creator).withdrawFunds(campaignId)
      ).to.be.revertedWith("Goal not reached");
    });
  });
  
  describe("Refunds", function () {
    let campaignId;
    const goal = ethers.parseEther("10");
    
    beforeEach(async function () {
      // Create campaign with deadline in the past
      const deadline = Math.floor(Date.now() / 1000) - 86400;
      await crowdfunding.connect(creator).createCampaign(goal, deadline);
      campaignId = 1;
    });
    
    it("Should allow refund if deadline passed and goal not met", async function () {
      const donationAmount = ethers.parseEther("5");
      await crowdfunding.connect(donor1).contribute(campaignId, { value: donationAmount });
      
      // Fast forward time (in real test, use time manipulation)
      // For now, we'll test the refund logic
      const initialBalance = await ethers.provider.getBalance(donor1.address);
      const tx = await crowdfunding.connect(donor1).refund(campaignId);
      const receipt = await tx.wait();
      const finalBalance = await ethers.provider.getBalance(donor1.address);
      
      expect(finalBalance - initialBalance).to.be.closeTo(donationAmount, ethers.parseEther("0.01"));
    });
  });
});
