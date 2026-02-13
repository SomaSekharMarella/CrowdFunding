import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <div className="navbar-content">
          <Link to="/dashboard" className="navbar-brand navbar-title">
            Crowdfunding Platform
          </Link>
          
          {user && (
            <div className="navbar-menu">
              <div className="nav-links">
                <Link to="/dashboard">Dashboard</Link>
                <Link to="/campaigns">Campaigns</Link>
                <Link to="/create-campaign">Create Campaign</Link>
                <Link to="/my-campaigns">My Campaigns</Link>
                <Link to="/my-donations">My Donations</Link>
                {user.roles?.includes('ROLE_ADMIN') && (
                  <>
                    <Link to="/admin">Admin</Link>
                    <Link to="/admin/users">Users</Link>
                    <Link to="/admin/campaigns">Campaigns</Link>
                  </>
                )}
              </div>
              <span className="navbar-user">{user.username}</span>
              <button onClick={handleLogout} className="btn btn-secondary logout-btn">
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
