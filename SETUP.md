# Setup Guide - Hybrid Blockchain Crowdfunding Platform

## Prerequisites

1. **Node.js** 18+ installed
2. **Java** 17+ installed
3. **MySQL** 8+ installed and running
4. **Maven** 3.6+ installed (or use Maven wrapper)
5. **MetaMask** browser extension installed
6. **Sepolia Testnet** ETH (get from faucet)

## Step-by-Step Setup

### 1. Database Setup

```sql
-- Open MySQL command line or MySQL Workbench
CREATE DATABASE crowdfunding_db;
```

### 2. Blockchain Setup

```bash
cd blockchain

# Install dependencies (already done)
npm install

# Create .env file
copy .env.example .env
# Or manually create .env with:
# SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/YOUR_PROJECT_ID
# PRIVATE_KEY=your_private_key_here

# Compile contracts
npm run compile

# Run tests (optional)
npm run test

# Deploy to Sepolia
npm run deploy:sepolia
```

**Important**: Save the deployed contract address!

### 3. Backend Setup

```bash
cd backend

# Update application.yml with:
# - MySQL credentials (username, password)
# - Blockchain RPC URL
# - Contract address (from deployment)

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

**Configuration in `application.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/crowdfunding_db
    username: root
    password: your_password

blockchain:
  network-url: https://sepolia.infura.io/v3/YOUR_PROJECT_ID
  contract-address: 0x... # From deployment
```

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies (already done)
npm install

# Create .env file
# Create .env with:
# VITE_CONTRACT_ADDRESS=0x... # From deployment

# Start development server
npm run dev
```

Frontend will start on `http://localhost:3000`

## Quick Start Commands

### Start All Services

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

**Terminal 3 - Blockchain (if needed):**
```bash
cd blockchain
npm run node  # For local testing
```

## First Time Setup Checklist

- [ ] MySQL database created
- [ ] Blockchain `.env` file created with RPC URL and private key
- [ ] Smart contract deployed to Sepolia
- [ ] Contract address saved
- [ ] Backend `application.yml` configured
- [ ] Frontend `.env` file created with contract address
- [ ] MetaMask installed and connected to Sepolia testnet
- [ ] Sepolia test ETH obtained from faucet

## Testing the Setup

1. **Backend Health Check:**
   - Open browser: `http://localhost:8080/api/campaigns`
   - Should return empty array `[]` (no campaigns yet)

2. **Frontend:**
   - Open browser: `http://localhost:3000`
   - Should see login page

3. **Create Account:**
   - Click "Sign up"
   - Create a test account
   - Login

4. **Connect Wallet:**
   - Click "Connect MetaMask"
   - Approve connection
   - Wallet address should appear

5. **Create Campaign:**
   - Click "Create Campaign"
   - Fill in details
   - Submit (will require MetaMask transaction)
   - Campaign should appear in list

## Troubleshooting

### Backend won't start
- Check MySQL is running
- Verify database credentials in `application.yml`
- Check port 8080 is not in use

### Frontend won't start
- Check Node.js version: `node --version` (should be 18+)
- Delete `node_modules` and run `npm install` again
- Check port 3000 is not in use

### MetaMask connection fails
- Ensure MetaMask is installed
- Switch to Sepolia testnet
- Check contract address in frontend `.env` matches deployed address

### Blockchain calls fail
- Verify RPC URL is correct
- Check contract address is correct
- Ensure you're on Sepolia testnet
- Check you have test ETH for gas fees

## Environment Variables Summary

### Blockchain (.env)
```
SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/YOUR_PROJECT_ID
PRIVATE_KEY=your_private_key_here
```

### Frontend (.env)
```
VITE_CONTRACT_ADDRESS=0x...
```

### Backend (application.yml)
```yaml
spring:
  datasource:
    username: root
    password: your_password

blockchain:
  network-url: https://sepolia.infura.io/v3/YOUR_PROJECT_ID
  contract-address: 0x...
```

## Next Steps

1. Create your first user account
2. Connect MetaMask wallet
3. Create a test campaign
4. Make a test donation
5. Explore all features!

---

**Note**: This is a testnet project. Use Sepolia ETH only. Never use mainnet or real funds.
