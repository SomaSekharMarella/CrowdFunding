# Quick Start Guide

## 🚀 Get Running in 5 Minutes

### Prerequisites Check
```bash
node --version    # Should be 18+
java -version     # Should be 17+
mysql --version   # Should be 8+
```

### 1. Database (30 seconds)
```sql
CREATE DATABASE crowdfunding_db;
```

### 2. Backend (2 minutes)
```bash
cd backend
# Edit application.yml with your MySQL password
mvn spring-boot:run
```

### 3. Frontend (1 minute)
```bash
cd frontend
# Create .env file with: VITE_CONTRACT_ADDRESS=0x...
npm run dev
```

### 4. Access
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

### 5. First Steps
1. Sign up for an account
2. Login
3. Connect MetaMask (Sepolia testnet)
4. Create a campaign!

---

**For full setup including blockchain deployment, see SETUP.md**
