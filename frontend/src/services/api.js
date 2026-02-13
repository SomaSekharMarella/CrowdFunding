import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    // Log 403 errors for debugging
    if (error.response?.status === 403) {
      console.error('403 Forbidden:', {
        url: error.config?.url,
        method: error.config?.method,
        hasAuthHeader: !!error.config?.headers?.Authorization,
        authHeader: error.config?.headers?.Authorization ? 
          error.config.headers.Authorization.substring(0, 20) + '...' : 'none',
        responseData: error.response?.data,
        status: error.response?.status
      });
    }
    
    // Log 401 errors
    if (error.response?.status === 401) {
      console.error('401 Unauthorized:', {
        url: error.config?.url,
        message: error.response?.data
      });
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  signup: (data) => api.post('/auth/signup', data),
  login: (data) => api.post('/auth/login', data)
};

export const walletAPI = {
  connect: (address) => api.post('/wallet/connect', { address }),
  getMyWallet: () => api.get('/wallet/my-wallet')
};

export const campaignAPI = {
  getAll: () => api.get('/campaigns'),
  getActive: () => api.get('/campaigns/active'),
  getById: (id) => api.get(`/campaigns/${id}`),
  createMetadata: (blockchainId, data) =>
    api.post(`/campaigns/metadata?blockchainId=${blockchainId}`, data),
  getMyCampaigns: () => api.get('/campaigns/my-campaigns'),
  sync: (id) => api.post(`/campaigns/${id}/sync`)
};

export const donationAPI = {
  record: (campaignId, transactionHash, amount, blockNumber) =>
    api.post('/donations/record', null, {
      params: { campaignId, transactionHash, amount, blockNumber }
    }),
  getMyDonations: () => api.get('/donations/my-donations'),
  getByCampaign: (campaignId) => api.get(`/donations/campaign/${campaignId}`)
};

export const adminAPI = {
  getUsers: () => api.get('/admin/users'),
  blockUser: (id) => api.put(`/admin/block/${id}`),
  unblockUser: (id) => api.put(`/admin/unblock/${id}`),
  getCampaigns: () => api.get('/admin/campaigns'),
  cancelCampaign: (id) => api.delete(`/admin/campaign/${id}`)
};

export default api;
