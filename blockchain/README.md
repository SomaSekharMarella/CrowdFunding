# Blockchain Crowdfunding - Smart Contracts

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create `.env` file:
```
SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/YOUR_PROJECT_ID
PRIVATE_KEY=your_private_key_here
```

3. Compile contracts:
```bash
npm run compile
```

4. Run tests:
```bash
npm run test
```

5. Deploy to Sepolia:
```bash
npm run deploy:sepolia
```

## Contract Address

After deployment, update the contract address in:
- `backend/src/main/resources/application.yml`
- `frontend/src/config/contract.js`
