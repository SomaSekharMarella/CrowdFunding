# Hybrid Blockchain-Based Crowdfunding Platform

Final Year B.Tech Project - A decentralized crowdfunding platform using hybrid architecture.

## 🏗️ Architecture

This project follows a **Hybrid Architecture** pattern:

- **Blockchain (Ethereum Sepolia)**: Handles financial operations, transparency, refunds, and trust
- **Spring Boot + MySQL**: Handles authentication, user management, campaign metadata, and UI data
- **React.js**: Frontend user interface
- **MetaMask**: Wallet interactions (all transactions sent from user wallet)

### Key Principles

✅ **Blockchain stores ONLY financial & trust-critical data**
- Campaign goals, deadlines, donations, withdrawals, refunds

✅ **MySQL stores ONLY non-financial, UI, and authentication data**
- User accounts, campaign descriptions, images, transaction references

✅ **All ETH transactions sent from user wallet (MetaMask)**
- Backend NEVER signs or sends blockchain transactions
- Backend only reads from blockchain

## 📁 Project Structure

```
blockchain-crowdfunding/
│
├── blockchain/              # Smart Contracts (Hardhat + Solidity)
│   ├── contracts/
│   │   └── Crowdfunding.sol
│   ├── scripts/
│   ├── test/
│   └── hardhat.config.js
│
├── backend/                 # Spring Boot API
│   ├── src/main/java/com/crowdfunding/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   └── config/
│   └── src/main/resources/
│       └── application.yml
│
└── frontend/                # React.js Frontend
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   ├── services/
    │   ├── context/
    │   └── config/
    └── package.json
```

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- Java 17+
- MySQL 8+
- MetaMask browser extension
- Sepolia testnet ETH

### 1. Blockchain Setup

```bash
cd blockchain
npm install

# Create .env file
echo "SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/YOUR_PROJECT_ID" > .env
echo "PRIVATE_KEY=your_private_key_here" >> .env

# Compile contracts
npm run compile

# Run tests
npm run test

# Deploy to Sepolia
npm run deploy:sepolia
```

**Note**: Save the deployed contract address for backend and frontend configuration.

### 2. Backend Setup

```bash
cd backend

# Create MySQL database
mysql -u root -p
CREATE DATABASE crowdfunding_db;

# Update application.yml with:
# - Database credentials
# - Blockchain RPC URL
# - Contract address (from deployment)

# Build and run
mvn clean install
mvn spring-boot:run
```

Backend will run on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd frontend
npm install

# Create .env file
echo "VITE_CONTRACT_ADDRESS=your_deployed_contract_address" > .env

# Start development server
npm run dev
```

Frontend will run on `http://localhost:3000`

## 📋 Features

### User Roles

1. **User (Donor)**
   - Signup & Login
   - Connect MetaMask wallet
   - View campaigns
   - Donate ETH
   - View donation history
   - Claim refund if campaign fails

2. **Campaign Creator**
   - All donor features +
   - Create crowdfunding campaign
   - Withdraw funds if goal met

3. **Admin** (Optional - Off-chain only)
   - View users & campaigns
   - Analytics dashboard
   - ❌ No control over funds

### Campaign Lifecycle

1. **Create Campaign**
   - Metadata saved in MySQL
   - Financial parameters saved on blockchain

2. **Donate**
   - Donor sends ETH via MetaMask
   - Smart contract records donation
   - Backend stores transaction hash

3. **Refund** (Automatic)
   - If deadline passed AND goal not met
   - Donors can call `refund()`
   - Smart contract sends ETH back

4. **Withdraw**
   - If goal met
   - Campaign creator can withdraw funds
   - ETH sent directly to creator wallet

## 🔐 Smart Contract Functions

- `createCampaign(goal, deadline)` - Create new campaign
- `contribute(campaignId)` - Donate ETH
- `withdrawFunds(campaignId)` - Withdraw funds (owner only)
- `refund(campaignId)` - Claim refund
- `getCampaign(campaignId)` - Get campaign details
- `isRefundable(campaignId)` - Check if refundable

## 🗄️ Database Schema

- **users** - User accounts (username, email, password)
- **wallets** - Wallet address mapping (one user = one wallet)
- **campaigns** - Campaign metadata (title, description, images)
- **donations** - Transaction references (transaction hash, amount)
- **roles** - User roles (USER, CREATOR, ADMIN)

## 🔧 Configuration

### Backend (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/crowdfunding_db
    username: root
    password: root

blockchain:
  network-url: https://sepolia.infura.io/v3/YOUR_PROJECT_ID
  contract-address: 0x...
  chain-id: 11155111
```

### Frontend (`.env`)

```
VITE_CONTRACT_ADDRESS=0x...
```

## 📝 API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login

### Wallet
- `POST /api/wallet/connect` - Connect MetaMask wallet
- `GET /api/wallet/my-wallet` - Get connected wallet

### Campaigns
- `GET /api/campaigns` - Get all campaigns
- `GET /api/campaigns/{id}` - Get campaign by ID
- `POST /api/campaigns/metadata` - Create campaign metadata
- `GET /api/campaigns/my-campaigns` - Get user's campaigns
- `POST /api/campaigns/{id}/sync` - Sync from blockchain

### Donations
- `POST /api/donations/record` - Record donation transaction
- `GET /api/donations/my-donations` - Get user's donations
- `GET /api/donations/campaign/{campaignId}` - Get campaign donations

## 🧪 Testing

### Smart Contracts
```bash
cd blockchain
npm run test
```

### Backend
```bash
cd backend
mvn test
```

## 📚 Technology Stack

- **Blockchain**: Solidity, Hardhat, Ethereum Sepolia
- **Backend**: Spring Boot 3.2, MySQL, JWT, Web3j
- **Frontend**: React 18, Vite, Ethers.js, Axios
- **Wallet**: MetaMask

## ⚠️ Important Notes

1. **Network**: Ethereum Sepolia Testnet only
2. **Currency**: ETH only (no ERC20 tokens)
3. **No Admin Fund Control**: Fully decentralized
4. **Backend is Read-Only**: Never signs transactions
5. **All Transactions**: Sent from user wallet via MetaMask

## 🎓 Project Constraints

- Simple, readable code
- Academic + Industry balance
- Exam-friendly structure
- Industry-grade appearance

## 📄 License

MIT License - Educational Project

## 👥 Authors

Final Year B.Tech Project

---

**Note**: This is a testnet project. Use Sepolia ETH only. Never use mainnet or real funds.
