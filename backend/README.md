# Blockchain Crowdfunding - Backend API

## Setup

1. Install MySQL and create database:
```sql
CREATE DATABASE crowdfunding_db;
```

2. Update `application.yml` with your database credentials

3. Update blockchain configuration:
   - `blockchain.network-url`: Sepolia RPC URL
   - `blockchain.contract-address`: Deployed contract address

4. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login

### Wallet
- `POST /api/wallet/connect` - Connect MetaMask wallet
- `GET /api/wallet/my-wallet` - Get connected wallet

### Campaigns
- `GET /api/campaigns` - Get all campaigns
- `GET /api/campaigns/{id}` - Get campaign by ID
- `POST /api/campaigns/metadata` - Create campaign metadata (after blockchain creation)
- `GET /api/campaigns/my-campaigns` - Get user's campaigns
- `POST /api/campaigns/{id}/sync` - Sync campaign from blockchain

### Donations
- `POST /api/donations/record` - Record donation transaction
- `GET /api/donations/my-donations` - Get user's donations
- `GET /api/donations/campaign/{campaignId}` - Get campaign donations

## Environment Variables

Set in `application.yml` or environment:
- `JWT_SECRET`: Secret key for JWT (minimum 32 characters)
- `BLOCKCHAIN_RPC_URL`: Ethereum Sepolia RPC URL
- `CROWDFUNDING_CONTRACT_ADDRESS`: Deployed contract address
