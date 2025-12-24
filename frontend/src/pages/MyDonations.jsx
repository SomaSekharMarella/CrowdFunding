import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { donationAPI } from '../services/api';
import './MyDonations.css';

const MyDonations = () => {
  const [donations, setDonations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDonations();
  }, []);

  const loadDonations = async () => {
    try {
      const response = await donationAPI.getMyDonations();
      setDonations(response.data);
    } catch (err) {
      setError('Failed to load donations');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="container">Loading donations...</div>;
  }

  return (
    <div className="container">
      <h1>My Donations</h1>
      {error && <div className="alert alert-error">{error}</div>}

      {donations.length === 0 ? (
        <div className="empty-state">
          <p>You haven't made any donations yet.</p>
          <Link to="/campaigns" className="btn btn-primary">
            Browse Campaigns
          </Link>
        </div>
      ) : (
        <div className="donations-list">
          {donations.map(donation => (
            <div key={donation.id} className="donation-card">
              <div className="donation-header">
                <h3>
                  <Link to={`/campaigns/${donation.campaignId}`}>
                    {donation.campaignTitle}
                  </Link>
                </h3>
                <span className="donation-amount">+{donation.amount?.toFixed(4)} ETH</span>
              </div>
              <div className="donation-details">
                <p className="donation-date">
                  Donated on {new Date(donation.donatedAt).toLocaleString()}
                </p>
                <a
                  href={`https://sepolia.etherscan.io/tx/${donation.transactionHash}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-secondary btn-sm"
                >
                  View on Etherscan
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyDonations;
