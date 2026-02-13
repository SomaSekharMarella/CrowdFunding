import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminAPI } from '../services/api';
import './Admin.css';

const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const res = await adminAPI.getUsers();
      setUsers(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(err.response?.data || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleBlock = async (id, username) => {
    if (!confirm(`Block user "${username}"? They will not be able to login or use the platform.`)) return;
    try {
      await adminAPI.blockUser(id);
      loadUsers();
    } catch (err) {
      alert(err.response?.data || 'Failed to block user');
    }
  };

  const handleUnblock = async (id, username) => {
    try {
      await adminAPI.unblockUser(id);
      loadUsers();
    } catch (err) {
      alert(err.response?.data || 'Failed to unblock user');
    }
  };

  if (loading) return <div className="container">Loading users...</div>;

  return (
    <div className="container">
      <div className="admin-header">
        <h1>All Users</h1>
        <Link to="/admin" className="btn btn-secondary">← Admin Dashboard</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Email</th>
              <th>Full Name</th>
              <th>Status</th>
              <th>Roles</th>
              <th>Wallet</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>{u.fullName || '-'}</td>
                <td>
                  <span className={`status-badge ${u.status === 'ACTIVE' ? 'success' : 'danger'}`}>
                    {u.status}
                  </span>
                </td>
                <td>{u.roles?.join(', ') || '-'}</td>
                <td className="wallet-cell">{u.walletAddress ? `${u.walletAddress.slice(0, 10)}...` : '-'}</td>
                <td>
                  {u.status === 'ACTIVE' ? (
                    <button
                      onClick={() => handleBlock(u.id, u.username)}
                      className="btn btn-danger btn-sm"
                      disabled={u.roles?.includes('ROLE_ADMIN')}
                      title={u.roles?.includes('ROLE_ADMIN') ? 'Cannot block admin' : 'Block user'}
                    >
                      Block
                    </button>
                  ) : (
                    <button
                      onClick={() => handleUnblock(u.id, u.username)}
                      className="btn btn-success btn-sm"
                    >
                      Unblock
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminUsers;
