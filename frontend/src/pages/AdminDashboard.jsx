import React from 'react';
import { Link } from 'react-router-dom';
import { adminAPI } from '../services/api';
import { useState, useEffect } from 'react';
import './Admin.css';

const AdminDashboard = () => {
  const [stats, setStats] = useState({ users: 0, campaigns: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [usersRes, campaignsRes] = await Promise.all([
          adminAPI.getUsers(),
          adminAPI.getCampaigns()
        ]);
        setStats({
          users: usersRes.data?.length ?? 0,
          campaigns: campaignsRes.data?.length ?? 0
        });
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div className="container">Loading...</div>;

  return (
    <div className="container">
      <h1>Admin Dashboard</h1>
      <div className="admin-stats">
        <div className="stat-card">
          <h3>Total Users</h3>
          <p className="stat-number">{stats.users}</p>
          <Link to="/admin/users" className="btn btn-primary btn-sm">Manage</Link>
        </div>
        <div className="stat-card">
          <h3>Total Campaigns</h3>
          <p className="stat-number">{stats.campaigns}</p>
          <Link to="/admin/campaigns" className="btn btn-primary btn-sm">Manage</Link>
        </div>
      </div>
      <div className="admin-actions">
        <Link to="/admin/users" className="btn btn-primary">View All Users</Link>
        <Link to="/admin/campaigns" className="btn btn-secondary">View All Campaigns</Link>
      </div>
    </div>
  );
};

export default AdminDashboard;
