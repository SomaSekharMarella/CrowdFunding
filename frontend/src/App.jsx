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
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
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
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
