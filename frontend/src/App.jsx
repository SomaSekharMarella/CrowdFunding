import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import CampaignList from './pages/CampaignList';
import CampaignDetails from './pages/CampaignDetails';
import CreateCampaign from './pages/CreateCampaign';
import MyDonations from './pages/MyDonations';
import MyCampaigns from './pages/MyCampaigns';
import AdminDashboard from './pages/AdminDashboard';
import AdminUsers from './pages/AdminUsers';
import AdminCampaigns from './pages/AdminCampaigns';
import AdminRoute from './components/AdminRoute';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App app-layout">
          <Navbar />
          <main className="main-content">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route
              path="/dashboard"
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              }
            />
            <Route
              path="/campaigns"
              element={
                <PrivateRoute>
                  <CampaignList />
                </PrivateRoute>
              }
            />
            <Route
              path="/campaigns/:id"
              element={
                <PrivateRoute>
                  <CampaignDetails />
                </PrivateRoute>
              }
            />
            <Route
              path="/create-campaign"
              element={
                <PrivateRoute>
                  <CreateCampaign />
                </PrivateRoute>
              }
            />
            <Route
              path="/my-donations"
              element={
                <PrivateRoute>
                  <MyDonations />
                </PrivateRoute>
              }
            />
            <Route
              path="/my-campaigns"
              element={
                <PrivateRoute>
                  <MyCampaigns />
                </PrivateRoute>
              }
            />
            <Route
              path="/admin"
              element={
                <PrivateRoute>
                  <AdminRoute>
                    <AdminDashboard />
                  </AdminRoute>
                </PrivateRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <PrivateRoute>
                  <AdminRoute>
                    <AdminUsers />
                  </AdminRoute>
                </PrivateRoute>
              }
            />
            <Route
              path="/admin/campaigns"
              element={
                <PrivateRoute>
                  <AdminRoute>
                    <AdminCampaigns />
                  </AdminRoute>
                </PrivateRoute>
              }
            />
          </Routes>
          </main>
          <footer className="app-footer footer">
            <div className="container app-footer-inner footer-content">
              <div>
                <div className="app-footer-title">Crowdfunding Platform</div>
                <div className="app-footer-meta">Hybrid blockchain + Spring Boot crowdfunding dashboard</div>
              </div>
              <div className="app-footer-meta">
                Built with Ethereum · Spring Boot · React
              </div>
            </div>
          </footer>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
