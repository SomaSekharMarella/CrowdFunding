import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { campaignAPI, donationAPI } from '../services/api';
import web3Service from '../services/web3';
import './CampaignDetails.css';

const CampaignDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [campaign, setCampaign] = useState(null);
  const [donations, setDonations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [donationAmount, setDonationAmount] = useState('');
  const [donating, setDonating] = useState(false);
  const [isRefundable, setIsRefundable] = useState(false);

  useEffect(() => {
    loadCampaign();
  }, [id]);

  useEffect(() => {
    if (campaign?.blockchainId) {
      checkRefundable();
    }
  }, [campaign]);

  const loadCampaign = async (showLoading = false, forceSync = false) => {
    if (showLoading) setLoading(true);
    try {
      // Optionally force sync before fetching (for manual refresh)
      if (forceSync) {
        try {
          await campaignAPI.sync(id);
        } catch (syncErr) {
          console.warn('Manual sync failed:', syncErr);
        }
      }
      
      // Backend getCampaignById automatically syncs from blockchain
      const [campaignRes, donationsRes] = await Promise.all([
        campaignAPI.getById(id),
        donationAPI.getByCampaign(id).catch(() => ({ data: [] }))
      ]);
      setCampaign(campaignRes?.data ?? null);
      setDonations(Array.isArray(donationsRes?.data) ? donationsRes.data : []);
      setError(''); // Clear any previous errors
    } catch (err) {
      setError(err.response?.status === 404 ? 'Campaign not found' : 'Failed to load campaign');
      if (err.response?.status !== 404) {
        setCampaign(null);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    await loadCampaign(true, true); // Show loading and force sync
  };

  const checkRefundable = async () => {
    try {
      if (campaign?.blockchainId) {
        const refundable = await web3Service.isRefundable(campaign.blockchainId);
        setIsRefundable(refundable);
      }
    } catch (err) {
      console.error('Error checking refundable:', err);
    }
  };

  const handleDonate = async () => {
    if (!donationAmount || parseFloat(donationAmount) <= 0) {
      alert('Please enter a valid donation amount');
      return;
    }

    setDonating(true);
    try {
      // Connect wallet if not connected
      await web3Service.connectWallet();

      // Donate on blockchain
      const result = await web3Service.donate(campaign.blockchainId, donationAmount);
      
      // Record donation in backend (this also syncs campaign data from blockchain)
      await donationAPI.record(
        campaign.id,
        result.transactionHash,
        donationAmount,
        result.blockNumber
      );

      alert('Donation successful! Transaction: ' + result.transactionHash);
      setDonationAmount('');
      
      // Reload campaign data to show updated progress (with sync)
      await loadCampaign(true);
    } catch (err) {
      alert('Donation failed: ' + err.message);
    } finally {
      setDonating(false);
    }
  };

  const handleWithdraw = async () => {
    if (!confirm('Are you sure you want to withdraw funds?')) return;

    try {
      await web3Service.connectWallet();
      const result = await web3Service.withdrawFunds(campaign.blockchainId);
      alert('Funds withdrawn successfully! Transaction: ' + result.transactionHash);
      loadCampaign(true);
    } catch (err) {
      alert('Withdrawal failed: ' + err.message);
    }
  };

  const handleRefund = async () => {
    if (!confirm('Are you sure you want to claim a refund?')) return;

    try {
      await web3Service.connectWallet();
      const result = await web3Service.refund(campaign.blockchainId);
      alert('Refund successful! Transaction: ' + result.transactionHash);
      loadCampaign(true);
    } catch (err) {
      alert('Refund failed: ' + err.message);
    }
  };

  if (loading) {
    return <div className="container">Loading campaign...</div>;
  }

  if (!campaign) {
    return <div className="container">Campaign not found</div>;
  }

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');
  const isOwner = user?.walletAddress?.toLowerCase() === campaign.creatorWalletAddress?.toLowerCase();
  const goalAmount = Number(campaign.goalAmount);
  const totalRaised = Number(campaign.totalRaised);
  const progress = goalAmount > 0 ? Math.min((totalRaised / goalAmount) * 100, 100) : 0;
  const canWithdraw = isOwner && campaign.goalReached && !campaign.fundsWithdrawn;
  const canDonate = !isAdmin && !isOwner;

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <button onClick={() => navigate(-1)} className="btn btn-secondary back-btn">
          ← Back
        </button>
        <button 
          onClick={handleRefresh} 
          className="btn btn-secondary"
          disabled={loading}
          title="Refresh campaign data from blockchain"
        >
          {loading ? 'Refreshing...' : '🔄 Refresh'}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="campaign-details">
        <div className="campaign-header">
          {campaign.imageUrl && (
            <img src={campaign.imageUrl} alt={campaign.title} className="campaign-hero-image" />
          )}
          <div className="campaign-info">
            <h1>{campaign.title}</h1>
            <p className="campaign-meta">
              By {campaign.creatorUsername} • Created {new Date(campaign.createdAt).toLocaleDateString()}
            </p>
            <p className="campaign-description">{campaign.description}</p>
          </div>
        </div>

        <div className="campaign-stats-section">
          <div className="stat-box">
            <h3>Total Raised</h3>
            <p className="stat-value">{campaign.totalRaised?.toFixed(4)} ETH</p>
          </div>
          <div className="stat-box">
            <h3>Goal</h3>
            <p className="stat-value">{campaign.goalAmount?.toFixed(4)} ETH</p>
          </div>
          <div className="stat-box">
            <h3>Progress</h3>
            <p className="stat-value">{progress.toFixed(1)}%</p>
          </div>
          <div className="stat-box">
            <h3>Deadline</h3>
            <p className="stat-value">{new Date(campaign.deadline).toLocaleDateString()}</p>
          </div>
        </div>

        <div className="progress-section">
          <div className="progress-bar-large">
            <div
              className="progress-fill-large"
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>
        </div>

        <div className="campaign-actions">
          {campaign.status && (
            <span className={`status-badge ${campaign.status === 'ACTIVE' ? 'active' : campaign.status === 'COMPLETED' ? 'success' : 'danger'}`}>
              {campaign.status}
            </span>
          )}
          {canDonate && (
            <div className="donation-section">
              <h3>Make a Donation</h3>
              <div className="donation-input">
                <input
                  type="number"
                  step="0.001"
                  min="0.001"
                  value={donationAmount}
                  onChange={(e) => setDonationAmount(e.target.value)}
                  placeholder="Amount in ETH"
                />
                <button
                  onClick={handleDonate}
                  className="btn btn-primary"
                  disabled={donating || campaign.fundsWithdrawn || new Date(campaign.deadline) < new Date()}
                >
                  {donating ? 'Processing...' : 'Donate'}
                </button>
              </div>
              {isRefundable && (
                <button onClick={handleRefund} className="btn btn-danger">
                  Claim Refund
                </button>
              )}
            </div>
          )}
          {isAdmin && !canDonate && !isOwner && (
            <p className="info-text">Admins cannot donate to campaigns.</p>
          )}
          {isOwner && (
            <div className="owner-actions">
              <h3>Campaign Owner Actions</h3>
              {canWithdraw && (
                <button onClick={handleWithdraw} className="btn btn-success">
                  Withdraw Funds
                </button>
              )}
              {campaign.fundsWithdrawn && (
                <p className="info-text">Funds have been withdrawn</p>
              )}
            </div>
          )}
        </div>

        <div className="donations-section">
          <h2>Recent Donations</h2>
          {donations.length === 0 ? (
            <p>No donations yet. Be the first to donate!</p>
          ) : (
            <div className="donations-list">
              {donations.map(donation => (
                <div key={donation.id} className="donation-item">
                  <div>
                    <p className="donation-amount">{donation.amount?.toFixed(4)} ETH</p>
                    <p className="donation-meta">
                      {donation.donorWalletAddress || 'Wallet not linked'} • {new Date(donation.donatedAt).toLocaleString()}
                    </p>
                  </div>
                  <a
                    href={`https://sepolia.etherscan.io/tx/${donation.transactionHash}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn btn-secondary btn-sm"
                  >
                    View on Etherscan
                  </a>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CampaignDetails;
