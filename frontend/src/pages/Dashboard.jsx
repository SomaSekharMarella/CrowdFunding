import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { campaignAPI, walletAPI } from '../services/api';
import api from '../services/api';
import web3Service from '../services/web3';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [campaigns, setCampaigns] = useState([]);
  const [walletConnected, setWalletConnected] = useState(false);
  const [walletAddress, setWalletAddress] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [campaignsRes, walletRes] = await Promise.all([
        campaignAPI.getAll(),
        walletAPI.getMyWallet().catch(() => null)
      ]);

      setCampaigns(campaignsRes.data?.slice(0, 6) ?? []);

      const walletData = walletRes?.data;
      if (walletData && typeof walletData === 'object' && walletData.address) {
        setWalletAddress(walletData.address);
        setWalletConnected(true);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const testAuth = async () => {
    try {
      const response = await api.get('/wallet/test-auth');
      console.log('Auth test result:', response.data);
      alert('Auth Test: ' + JSON.stringify(response.data, null, 2));
    } catch (err) {
      console.error('Auth test error:', err);
      alert('Auth Test Failed: ' + (err.response?.data || err.message));
    }
  };

  const handleConnectWallet = async () => {
    try {
      // Check if user is logged in
      if (!user) {
        alert('Please login first before connecting wallet');
        return;
      }

      // Check if token exists
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Session expired. Please login again.');
        window.location.href = '/login';
        return;
      }

      console.log('Token exists:', !!token, 'Token length:', token?.length);
      console.log('User:', user);

      // Test authentication first
      try {
        const authTest = await api.get('/wallet/test-auth');
        console.log('Auth test before wallet connect:', authTest.data);
        console.log('Auth test details:', JSON.stringify(authTest.data, null, 2));
        
        // Check if authentication is valid
        if (!authTest.data.authenticated) {
          alert('Authentication failed. Please login again.');
          window.location.href = '/login';
          return;
        }
        
        // Check if principal type is UserPrincipal (can be full class name or simple name)
        const principalType = authTest.data.principalType || '';
        if (!principalType.includes('UserPrincipal')) {
          alert('Invalid authentication type: ' + principalType);
          return;
        }
        
        console.log('Authentication validated successfully');
      } catch (authErr) {
        console.error('Auth test failed:', authErr);
        console.error('Auth test error details:', {
          status: authErr.response?.status,
          data: authErr.response?.data,
          message: authErr.message
        });
        alert('Authentication check failed. Please login again.');
        window.location.href = '/login';
        return;
      }

      // Connect MetaMask first
      const address = await web3Service.connectWallet();
      console.log('MetaMask connected, address:', address);

      // Then save to backend
      try {
        console.log('Sending wallet connect request with address:', address);
        console.log('Request payload:', { address });
        
        const response = await walletAPI.connect(address);
        console.log('Wallet connect response:', response);
        console.log('Wallet connect success!');
        setWalletAddress(address);
        setWalletConnected(true);
        alert('Wallet connected successfully!');
        // Reload to update wallet status
        loadData();
      } catch (apiError) {
        console.error('Backend API error:', apiError);
        console.error('Full error object:', apiError);
        console.error('Error response:', apiError.response);
        console.error('Error details:', {
          status: apiError.response?.status,
          statusText: apiError.response?.statusText,
          data: apiError.response?.data,
          headers: apiError.response?.headers,
          requestHeaders: apiError.config?.headers,
          url: apiError.config?.url,
          method: apiError.config?.method
        });
        
        // Try to get the actual error message
        let errorMessage = 'Access denied';
        if (typeof apiError.response?.data === 'string') {
          errorMessage = apiError.response.data;
        } else if (apiError.response?.data) {
          errorMessage = JSON.stringify(apiError.response.data);
        }
        
        console.error('Error message:', errorMessage);
        
        if (apiError.response?.status === 403) {
          alert('Access denied (403):\n\n' + errorMessage + '\n\nPlease try:\n1. Click "Test Auth" button to check authentication\n2. Logout and login again\n3. Check browser console for details');
        } else if (apiError.response?.status === 401) {
          alert('Session expired (401). Please login again.');
          window.location.href = '/login';
        } else {
          alert('Failed to save wallet to backend: ' + errorMessage);
        }
      }
    } catch (err) {
      console.error('Wallet connection error:', err);
      if (err.message.includes('MetaMask')) {
        alert('MetaMask Error: ' + err.message);
      } else {
        alert('Failed to connect wallet: ' + err.message);
      }
    }
  };

  if (loading) {
    return <div className="container">Loading...</div>;
  }

  return (
    <div className="container">
      <div className="dashboard-header">
        <h1>Welcome, {user?.username}!</h1>
        {!walletConnected && (
          <div className="wallet-section">
            <p>Connect your MetaMask wallet to start donating or creating campaigns</p>
            <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
              <button onClick={handleConnectWallet} className="btn btn-primary">
                Connect MetaMask
              </button>
              <button onClick={testAuth} className="btn btn-secondary" style={{ fontSize: '12px', padding: '8px 12px' }}>
                Test Auth
              </button>
            </div>
          </div>
        )}
        {walletConnected && (
          <div className="wallet-info">
            <p>Wallet Connected: <code>{walletAddress}</code></p>
          </div>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="dashboard-stats">
        <div className="stat-card">
          <h3>Total Campaigns</h3>
          <p className="stat-number">{campaigns.length}</p>
        </div>
        <div className="stat-card">
          <h3>Active Campaigns</h3>
          <p className="stat-number">
            {campaigns.filter(c => !c.goalReached && new Date(c.deadline) > new Date()).length}
          </p>
        </div>
      </div>

      <div className="dashboard-actions">
        <Link to="/create-campaign" className="btn btn-primary">
          Create Campaign
        </Link>
        <Link to="/campaigns" className="btn btn-secondary">
          View All Campaigns
        </Link>
      </div>

      <div className="recent-campaigns">
        <h2>Recent Campaigns</h2>
        {campaigns.length === 0 ? (
          <p>No campaigns yet. Be the first to create one!</p>
        ) : (
          <div className="campaign-grid">
            {campaigns.map(campaign => (
              <Link key={campaign.id} to={`/campaigns/${campaign.id}`} className="campaign-card">
                <h3>{campaign.title}</h3>
                <p className="campaign-description">{campaign.description?.substring(0, 100)}...</p>
                <div className="campaign-progress">
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
                  <p>
                    {campaign.totalRaised?.toFixed(4)} ETH / {campaign.goalAmount?.toFixed(4)} ETH
                  </p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
