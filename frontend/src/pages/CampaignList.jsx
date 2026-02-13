import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { campaignAPI } from '../services/api';
import './CampaignList.css';

const CampaignList = () => {
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadCampaigns();
  }, []);

  const loadCampaigns = async () => {
    try {
      const response = await campaignAPI.getActive();
      setCampaigns(Array.isArray(response?.data) ? response.data : []);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to load campaigns');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="container">Loading campaigns...</div>;
  }

  return (
    <div className="container">
      <div className="campaign-list-header">
        <h1>All Campaigns</h1>
        <Link to="/create-campaign" className="btn btn-primary">
          Create Campaign
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {campaigns.length === 0 ? (
        <div className="empty-state">
          <p>No campaigns found. Be the first to create one!</p>
          <Link to="/create-campaign" className="btn btn-primary">
            Create Campaign
          </Link>
        </div>
      ) : (
        <div className="campaign-list">
          {campaigns.map(campaign => (
            <div key={campaign.id} className="campaign-item">
              {campaign.imageUrl && (
                <img src={campaign.imageUrl} alt={campaign.title} className="campaign-image" />
              )}
              <div className="campaign-content">
                <h2>
                  <Link to={`/campaigns/${campaign.id}`}>{campaign.title}</Link>
                </h2>
                <p className="campaign-meta">
                  By {campaign.creatorUsername} • {new Date(campaign.createdAt).toLocaleDateString()}
                </p>
                <p className="campaign-description">{campaign.description}</p>
                <div className="campaign-stats">
                  <div className="stat">
                    <span className="stat-label">Raised:</span>
                    <span className="stat-value">{campaign.totalRaised?.toFixed(4)} ETH</span>
                  </div>
                  <div className="stat">
                    <span className="stat-label">Goal:</span>
                    <span className="stat-value">{campaign.goalAmount?.toFixed(4)} ETH</span>
                  </div>
                  <div className="stat">
                    <span className="stat-label">Progress:</span>
                    <span className="stat-value">
                      {campaign.goalAmount && Number(campaign.goalAmount) > 0
                        ? ((Number(campaign.totalRaised) / Number(campaign.goalAmount)) * 100).toFixed(1)
                        : '0'}%
                    </span>
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
                <div className="campaign-footer">
                  <span className={`status-badge ${campaign.goalReached ? 'success' : 'active'}`}>
                    {campaign.goalReached ? 'Goal Reached' : 'Active'}
                  </span>
                  <Link to={`/campaigns/${campaign.id}`} className="btn btn-primary btn-sm">
                    View Details
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CampaignList;
