import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminAPI } from '../services/api';
import web3Service from '../services/web3';
import './Admin.css';

const AdminCampaigns = () => {
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    loadCampaigns();
  }, []);

  const loadCampaigns = async () => {
    try {
      const res = await adminAPI.getCampaigns();
      setCampaigns(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(err.response?.data || 'Failed to load campaigns');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelInDb = async (id) => {
    if (!confirm('Mark this campaign as CANCELLED in the database?')) return;
    try {
      await adminAPI.cancelCampaign(id);
      loadCampaigns();
    } catch (err) {
      alert(err.response?.data || 'Failed to cancel campaign');
    }
  };

  const handleCancelOnChain = async (c) => {
    if (!confirm(`Cancel campaign #${c.blockchainId} on blockchain? Only the contract owner wallet can do this.`)) return;
    setCancellingId(c.id);
    try {
      await web3Service.connectWallet();
      await web3Service.cancelCampaign(c.blockchainId);
      alert('Campaign cancelled on blockchain.');
      loadCampaigns();
    } catch (err) {
      alert('On-chain cancel failed: ' + (err.message || err.reason || 'Only contract owner can call cancelCampaign.'));
    } finally {
      setCancellingId(null);
    }
  };

  if (loading) return <div className="container">Loading campaigns...</div>;

  return (
    <div className="container">
      <div className="admin-header">
        <h1>All Campaigns</h1>
        <Link to="/admin" className="btn btn-secondary">← Admin Dashboard</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="admin-campaigns-list">
        {campaigns.map((c) => (
          <div key={c.id} className="admin-campaign-card">
            <div className="admin-campaign-main">
              <h3><Link to={`/campaigns/${c.id}`}>{c.title}</Link></h3>
              <p className="admin-campaign-meta">
                By {c.creatorUsername} • ID: {c.id} (chain: {c.blockchainId}) • {c.totalRaised?.toFixed(4)} / {c.goalAmount?.toFixed(4)} ETH
              </p>
              <span className={`status-badge ${c.status === 'ACTIVE' ? 'active' : c.status === 'COMPLETED' ? 'success' : 'danger'}`}>
                {c.status}
              </span>
            </div>
            <div className="admin-campaign-actions">
              {c.status === 'ACTIVE' && (
                <>
                  <button
                    onClick={() => handleCancelInDb(c.id)}
                    className="btn btn-secondary btn-sm"
                  >
                    Cancel in DB
                  </button>
                  <button
                    onClick={() => handleCancelOnChain(c)}
                    className="btn btn-danger btn-sm"
                    disabled={cancellingId === c.id}
                  >
                    {cancellingId === c.id ? '...' : 'Cancel on chain'}
                  </button>
                </>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminCampaigns;
