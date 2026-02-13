import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { campaignAPI } from '../services/api';
import web3Service from '../services/web3';
import './CreateCampaign.css';

const CreateCampaign = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    imageUrl: '',
    category: '',
    goalAmount: '',
    deadline: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Validate form
      if (!formData.title || !formData.goalAmount || !formData.deadline) {
        throw new Error('Please fill in all required fields');
      }

      const deadlineDate = new Date(formData.deadline);
      if (deadlineDate <= new Date()) {
        throw new Error('Deadline must be in the future');
      }

      // Connect wallet
      await web3Service.connectWallet();

      // Convert deadline to Unix timestamp
      const deadlineTimestamp = Math.floor(deadlineDate.getTime() / 1000);

      // Create campaign on blockchain first
      const blockchainResult = await web3Service.createCampaign(
        formData.goalAmount,
        deadlineTimestamp
      );

      // Normalize deadline for backend (LocalDateTime expects optional seconds)
      const deadlineStr = formData.deadline && formData.deadline.length === 16
        ? formData.deadline + ':00'
        : formData.deadline;

      await campaignAPI.createMetadata(blockchainResult.campaignId, {
        title: formData.title,
        description: formData.description,
        imageUrl: formData.imageUrl || null,
        category: formData.category || null,
        goalAmount: formData.goalAmount,
        deadline: deadlineStr
      });

      alert('Campaign created successfully! Campaign ID: ' + blockchainResult.campaignId);
      navigate('/campaigns');
    } catch (err) {
      setError(err.message || 'Failed to create campaign');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="create-campaign">
        <h1>Create Campaign</h1>
        {error && <div className="alert alert-error">{error}</div>}
        
        <form onSubmit={handleSubmit} className="campaign-form">
          <div className="form-group">
            <label>Campaign Title *</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              required
              placeholder="Enter campaign title"
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="6"
              placeholder="Describe your campaign..."
            />
          </div>

          <div className="form-group">
            <label>Image URL</label>
            <input
              type="url"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/image.jpg"
            />
          </div>

          <div className="form-group">
            <label>Category</label>
            <input
              type="text"
              name="category"
              value={formData.category}
              onChange={handleChange}
              placeholder="e.g., Technology, Art, Education"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Goal Amount (ETH) *</label>
              <input
                type="number"
                step="0.001"
                min="0.001"
                name="goalAmount"
                value={formData.goalAmount}
                onChange={handleChange}
                required
                placeholder="10.0"
              />
            </div>

            <div className="form-group">
              <label>Deadline *</label>
              <input
                type="datetime-local"
                name="deadline"
                value={formData.deadline}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating Campaign...' : 'Create Campaign'}
            </button>
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="btn btn-secondary"
            >
              Cancel
            </button>
          </div>
        </form>

        <div className="info-box">
          <h3>Important Notes:</h3>
          <ul>
            <li>You need to connect your MetaMask wallet to create a campaign</li>
            <li>The campaign will be created on the Ethereum Sepolia testnet</li>
            <li>You will need to pay gas fees for the transaction</li>
            <li>Once created, you can withdraw funds if the goal is reached</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default CreateCampaign;
