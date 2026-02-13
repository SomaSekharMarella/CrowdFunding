import { ethers } from 'ethers';
import { CROWDFUNDING_ABI, CONTRACT_ADDRESS, NETWORK_CONFIG } from '../config/contract';

/**
 * Web3 Service - Handles MetaMask and blockchain interactions
 */
class Web3Service {
  constructor() {
    this.provider = null;
    this.signer = null;
    this.contract = null;
    this.account = null;
  }

  /**
   * Check if MetaMask is installed
   */
  isMetaMaskInstalled() {
    return typeof window.ethereum !== 'undefined';
  }

  /**
   * Connect to MetaMask
   */
  async connectWallet() {
    if (!this.isMetaMaskInstalled()) {
      throw new Error('MetaMask is not installed. Please install MetaMask extension.');
    }

    try {
      // Request account access
      const accounts = await window.ethereum.request({
        method: 'eth_requestAccounts'
      });

      if (accounts.length === 0) {
        throw new Error('No accounts found');
      }

      this.account = accounts[0];

      // Check network
      const chainId = await window.ethereum.request({ method: 'eth_chainId' });
      if (chainId !== NETWORK_CONFIG.chainId) {
        // Try to switch network
        try {
          await window.ethereum.request({
            method: 'wallet_switchEthereumChain',
            params: [{ chainId: NETWORK_CONFIG.chainId }]
          });
        } catch (switchError) {
          // Network doesn't exist, add it
          if (switchError.code === 4902) {
            await window.ethereum.request({
              method: 'wallet_addEthereumChain',
              params: [NETWORK_CONFIG]
            });
          } else {
            throw switchError;
          }
        }
      }

      // Setup provider and signer
      this.provider = new ethers.BrowserProvider(window.ethereum);
      this.signer = await this.provider.getSigner();
      this.contract = new ethers.Contract(CONTRACT_ADDRESS, CROWDFUNDING_ABI, this.signer);

      return this.account;
    } catch (error) {
      console.error('Error connecting wallet:', error);
      throw error;
    }
  }

  /**
   * Get current account
   */
  async getCurrentAccount() {
    if (!this.isMetaMaskInstalled()) {
      return null;
    }

    try {
      const accounts = await window.ethereum.request({ method: 'eth_accounts' });
      if (accounts.length > 0) {
        this.account = accounts[0];
        if (!this.provider) {
          this.provider = new ethers.BrowserProvider(window.ethereum);
          this.signer = await this.provider.getSigner();
          this.contract = new ethers.Contract(CONTRACT_ADDRESS, CROWDFUNDING_ABI, this.signer);
        }
        return this.account;
      }
      return null;
    } catch (error) {
      console.error('Error getting account:', error);
      return null;
    }
  }

  /**
   * Create campaign on blockchain
   */
  async createCampaign(goalInEth, deadlineTimestamp) {
    if (!this.contract) {
      await this.connectWallet();
    }

    try {
      const goalInWei = ethers.parseEther(goalInEth.toString());
      const tx = await this.contract.createCampaign(goalInWei, deadlineTimestamp);
      const receipt = await tx.wait();
      
      // Extract campaign ID from events
      const event = receipt.logs.find(log => {
        try {
          const parsed = this.contract.interface.parseLog(log);
          return parsed.name === 'CampaignCreated';
        } catch {
          return false;
        }
      });

      if (event) {
        const parsed = this.contract.interface.parseLog(event);
        const campaignId = parsed.args.campaignId.toString();
        return { campaignId, transactionHash: receipt.hash };
      }

      throw new Error('CampaignCreated event not found');
    } catch (error) {
      console.error('Error creating campaign:', error);
      throw error;
    }
  }

  /**
   * Donate to campaign
   */
  async donate(campaignId, amountInEth) {
    if (!this.contract) {
      await this.connectWallet();
    }

    try {
      const amountInWei = ethers.parseEther(amountInEth.toString());
      const tx = await this.contract.contribute(campaignId, { value: amountInWei });
      const receipt = await tx.wait();
      
      return {
        transactionHash: receipt.hash,
        blockNumber: receipt.blockNumber
      };
    } catch (error) {
      console.error('Error donating:', error);
      throw error;
    }
  }

  /**
   * Withdraw funds (campaign owner)
   */
  async withdrawFunds(campaignId) {
    if (!this.contract) {
      await this.connectWallet();
    }

    try {
      const tx = await this.contract.withdrawFunds(campaignId);
      const receipt = await tx.wait();
      return { transactionHash: receipt.hash };
    } catch (error) {
      console.error('Error withdrawing funds:', error);
      throw error;
    }
  }

  /**
   * Claim refund
   */
  async refund(campaignId) {
    if (!this.contract) {
      await this.connectWallet();
    }

    try {
      const tx = await this.contract.refund(campaignId);
      const receipt = await tx.wait();
      return { transactionHash: receipt.hash };
    } catch (error) {
      console.error('Error claiming refund:', error);
      throw error;
    }
  }

  /**
   * Get campaign data from blockchain
   */
  async getCampaign(campaignId) {
    if (!this.contract) {
      if (!this.provider) {
        this.provider = new ethers.BrowserProvider(window.ethereum);
      }
      this.contract = new ethers.Contract(CONTRACT_ADDRESS, CROWDFUNDING_ABI, this.provider);
    }

    try {
      const campaign = await this.contract.getCampaign(campaignId);
      return {
        owner: campaign[0],
        goal: campaign[1].toString(),
        deadline: campaign[2].toString(),
        totalRaised: campaign[3].toString(),
        goalReached: campaign[4],
        fundsWithdrawn: campaign[5],
        active: campaign[6]
      };
    } catch (error) {
      console.error('Error getting campaign:', error);
      throw error;
    }
  }

  /**
   * Cancel campaign (only contract owner / deployer wallet)
   */
  async cancelCampaign(campaignId) {
    if (!this.contract) await this.connectWallet();
    const tx = await this.contract.cancelCampaign(campaignId);
    const receipt = await tx.wait();
    return { transactionHash: receipt.hash };
  }

  /**
   * Check if campaign is refundable
   */
  async isRefundable(campaignId) {
    if (!this.contract) {
      if (!this.provider) {
        this.provider = new ethers.BrowserProvider(window.ethereum);
      }
      this.contract = new ethers.Contract(CONTRACT_ADDRESS, CROWDFUNDING_ABI, this.provider);
    }

    try {
      return await this.contract.isRefundable(campaignId);
    } catch (error) {
      console.error('Error checking refundable:', error);
      return false;
    }
  }

  /**
   * Convert Wei to ETH
   */
  formatEther(wei) {
    return ethers.formatEther(wei);
  }

  /**
   * Convert ETH to Wei
   */
  parseEther(eth) {
    return ethers.parseEther(eth.toString());
  }
}

export default new Web3Service();
