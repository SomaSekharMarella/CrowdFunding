import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { campaignAPI } from '../services/api';
import './MyCampaigns.css';

const MyCampaigns = () => {
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadCampaigns();
  }, []);

  const loadCampaigns = async () => {
    try {
      const response = await campaignAPI.getMyCampaigns();
      setCampaigns(response.data);
    } catch (err) {
      setError('Failed to load campaigns');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="container">Loading campaigns...</div>;
  }

  return (
    <div className="container">
      <div className="page-header my-campaigns-header">
        <h1 className="page-title">My Campaigns</h1>
        <Link to="/create-campaign" className="btn btn-primary primary-btn">
          Create New Campaign
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {campaigns.length === 0 ? (
        <div className="empty-card">
          <p className="empty-text">You haven't created any campaigns yet.</p>
          <Link to="/create-campaign" className="btn btn-primary primary-btn">
            Create Your First Campaign
          </Link>
        </div>
      ) : (
        <div className="campaigns-grid">
          {campaigns.map(campaign => (
            <div key={campaign.id} className="campaign-card">
              {campaign.imageUrl && (
                <img src={campaign.imageUrl} alt={campaign.title} className="campaign-thumbnail" />
              )}
              <div className="campaign-card-content">
                <h3>
                  <Link to={`/campaigns/${campaign.id}`}>{campaign.title}</Link>
                </h3>
                <p className="campaign-description">{campaign.description?.substring(0, 100)}...</p>
                <div className="campaign-stats">
                  <div className="stat">
                    <span className="stat-label">Raised</span>
                    <span className="stat-value">{campaign.totalRaised?.toFixed(4)} ETH</span>
                  </div>
                  <div className="stat">
                    <span className="stat-label">Goal</span>
                    <span className="stat-value">{campaign.goalAmount?.toFixed(4)} ETH</span>
                  </div>
                </div>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${campaign.goalAmount && Number(campaign.goalAmount) > 0
                        ? Math.min((Number(campaign.totalRaised) / Number(campaign.goalAmount)) * 100, 100)
                        : 0}%`
                    }}
                  />
                </div>
                <div className="campaign-status">
                  <span className={`status-badge ${campaign.goalReached ? 'success' : 'active'}`}>
                    {campaign.goalReached ? 'Goal Reached' : 'Active'}
                  </span>
                  {campaign.fundsWithdrawn && (
                    <span className="status-badge withdrawn">Funds Withdrawn</span>
                  )}
                </div>
                <Link to={`/campaigns/${campaign.id}`} className="btn btn-primary btn-sm">
                  View Details
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyCampaigns;
