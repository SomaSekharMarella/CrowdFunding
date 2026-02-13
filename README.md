Hybrid Blockchain-Based Crowdfunding Platform
A full-stack hybrid crowdfunding platform that combines Ethereum (Sepolia) smart contracts with a Spring Boot + MySQL backend and a React frontend.
All money movement and trust logic are on-chain; all UI, auth, and non‑critical data are off‑chain.
Table of Contents
Overview
Architecture
Main Features
Technology Stack
Project Structure
Setup & Running
Backend (Spring Boot)
Blockchain (Hardhat + Solidity)
Frontend (React + Vite)
Authentication & Authorization
Database Model
Smart Contract Design
REST API Overview
Frontend UX Flow
Troubleshooting & Common Issues
1. Overview
Goal: Build a decentralized crowdfunding system where:
Blockchain provides trust, transparency, and tamper‑proof handling of funds.
Backend provides an easy, secure REST API for:
user accounts,
campaign metadata,
transaction references,
wallet linking.
Frontend provides a modern, user-friendly UI for:
donors,
campaign creators.
Key idea:
ETH and campaign funds are always handled by the smart contract and MetaMask.
MySQL never stores actual balances, only metadata and references (e.g., transaction hashes).
2. Architecture
2.1 High-Level Components
Blockchain (Ethereum Sepolia)
Smart contract Crowdfunding.sol deployed using Hardhat.
Stores: goals, deadlines, raised amount, withdrawals, refunds.
Enforces: who can withdraw, who can refund, deadlines, goal checks.
Backend (Spring Boot + MySQL + JWT)
REST API under /api/**.
Handles:
Authentication & authorization (JWT).
Users, roles (ROLE_USER, ROLE_CREATOR, ROLE_ADMIN).
Wallet mapping (user → wallet address).
Campaign metadata (title, description, images, category, goal in ETH for display).
Donation references (transaction hash, amount).
Reads blockchain state using Web3j but never sends transactions.
Frontend (React + Vite)
SPA with protected routes.
Uses MetaMask (via web3.js) for blockchain interactions.
Uses Axios (services/api.js) for backend calls.
Uses React Context (AuthContext.jsx) for auth/session state.
2.2 Hybrid Design Principles
Blockchain stores ONLY trust‑critical data:
campaign financial parameters,
donation amounts,
goal reached status,
withdrawals, refunds.
MySQL stores ONLY non‑financial / UI data:
user profiles,
roles,
campaign descriptions, images, categories,
donation references (tx hash & metadata),
wallet address mappings.
All ETH transfers:
initiated from the user’s MetaMask wallet,
go directly to the smart contract,
or from contract back to the creator/donor.
Backend never holds private keys and never signs transactions.
3. Main Features
3.1 User Roles
User / Donor
Signup & login.
Connect MetaMask wallet.
Browse all campaigns.
Donate ETH to campaigns.
View personal donation history.
Claim refunds when a campaign fails.
Campaign Creator
Everything a donor can do, plus:
Create new campaigns (off-chain metadata + on-chain financial data).
Withdraw funds from successful campaigns.
Admin (off-chain only, optional)
View system-level information (users/campaigns).
No on-chain power; cannot move funds.
3.2 Campaign Lifecycle
Create campaign (Creator)
On-chain: campaign created via smart contract.
Off-chain: metadata saved via /api/campaigns/metadata.
Donate (User)
User connects MetaMask, sends ETH to contract.
Backend records transactionHash, amount, and campaign id via /api/donations/record.
Goal Check
Smart contract tracks:
total raised,
whether goal reached,
whether funds withdrawn.
Withdraw (Creator)
If goal reached before deadline:
creator calls withdrawFunds(campaignId) on-chain,
funds transferred from contract to creator’s wallet.
Refund (Donor)
If deadline passed and goal not reached:
donors call refund(campaignId) on-chain,
contract sends back their contribution.
4. Technology Stack
Blockchain
Solidity
Hardhat
Ethereum Sepolia testnet
Backend
Spring Boot 3.2
Java 17+
MySQL 8
Spring Security (JWT + stateless)
JPA / Hibernate
Web3j (for blockchain reads)
Frontend
React 18
Vite
React Router
Axios
Ethers/Web3 (via web3.js)
MetaMask
5. Project Structure
blockchain-crowdfunding/│├── blockchain/                      # Smart contracts (Hardhat)│   ├── contracts/│   │   └── Crowdfunding.sol│   ├── scripts/│   ├── test/│   ├── hardhat.config.js│   ├── package.json│   └── README.md│├── backend/                         # Spring Boot backend API│   ├── pom.xml│   ├── src/main/java/com/crowdfunding/│   │   ├── CrowdfundingApplication.java│   │   ├── config/│   │   │   ├── SecurityConfig.java│   │   │   ├── DataInitializer.java│   │   │   └── GlobalExceptionHandler.java│   │   ├── controller/│   │   │   ├── AuthController.java│   │   │   ├── WalletController.java│   │   │   ├── CampaignController.java│   │   │   └── DonationController.java│   │   ├── dto/│   │   │   ├── LoginRequest.java│   │   │   ├── SignupRequest.java│   │   │   ├── JwtResponse.java│   │   │   ├── CampaignCreateRequest.java│   │   │   ├── CampaignResponse.java│   │   │   ├── DonationResponse.java│   │   │   ├── WalletConnectRequest.java│   │   │   └── WalletResponse.java│   │   ├── entity/│   │   │   ├── User.java│   │   │   ├── Role.java│   │   │   ├── Wallet.java│   │   │   ├── Campaign.java│   │   │   └── Donation.java│   │   ├── repository/│   │   ├── security/│   │   │   ├── JwtAuthenticationFilter.java│   │   │   ├── UserDetailsServiceImpl.java│   │   │   └── UserPrincipal.java│   │   └── service/│   │       ├── UserService.java│   │       ├── JwtService.java│   │       ├── CampaignService.java│   │       ├── DonationService.java│   │       └── BlockchainService.java│   └── src/main/resources/│       └── application.yml│└── frontend/                        # React frontend    ├── package.json    ├── src/    │   ├── main.jsx    │   ├── App.jsx    │   ├── components/    │   │   ├── Navbar.jsx    │   │   └── PrivateRoute.jsx    │   ├── context/    │   │   └── AuthContext.jsx    │   ├── pages/    │   │   ├── Login.jsx    │   │   ├── Signup.jsx    │   │   ├── Dashboard.jsx    │   │   ├── CampaignList.jsx    │   │   ├── CampaignDetails.jsx    │   │   ├── CreateCampaign.jsx    │   │   ├── MyDonations.jsx    │   │   └── MyCampaigns.jsx    │   ├── services/    │   │   ├── api.js    │   │   └── web3.js    │   └── config/    │       └── contract.js    └── README.md
6. Setup & Running
6.1 Prerequisites
Node.js 18+
Java 17+
MySQL 8+
MetaMask browser extension
Sepolia testnet ETH (from a faucet)
6.2 Blockchain (Hardhat)
From the blockchain/ directory:
Install dependencies
cd blockchainnpm install
Configure environment
Create .env in blockchain/:
SEPOLIA_RPC_URL=https://sepolia.infura.io/v3/YOUR_PROJECT_IDPRIVATE_KEY=your_private_key_here
> Important: Use a test wallet/private key with Sepolia test ETH only.
Compile & test
npm run compilenpm run test
Deploy to Sepolia
npm run deploy:sepolia
Note the deployed contract address (e.g. 0x...).
You will use this in both backend and frontend configs.
6.3 Backend (Spring Boot API)
From the backend/ directory:
Create MySQL database
CREATE DATABASE crowdfunding_db;
Configure application.yml
File: backend/src/main/resources/application.yml
Database
spring:  datasource:    url: jdbc:mysql://localhost:3306/crowdfunding_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC    username: root        # your MySQL user    password: your_pass   # your MySQL password    driver-class-name: com.mysql.cj.jdbc.Driver
JPA
  jpa:    hibernate:      ddl-auto: update    show-sql: true    properties:      hibernate:        dialect: org.hibernate.dialect.MySQL8Dialect        format_sql: true
JWT & blockchain
  security:    jwt:      secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production-minimum-32-characters}      expiration: 86400000  # 24 hours
blockchain:  network-url: ${BLOCKCHAIN_RPC_URL:https://...your_sepolia_rpc...}  contract-address: ${CROWDFUNDING_CONTRACT_ADDRESS:0xYourDeployedAddress}  chain-id: 11155111
Server & CORS
server:  port: 8080  cors:    allowed-origins: http://localhost:3000    allowed-methods: GET,POST,PUT,DELETE,OPTIONS    allowed-headers: "*"    allow-credentials: true
Build & run
cd backendmvn clean installmvn spring-boot:run
Backend will be available at: http://localhost:8080
> If you get “Port 8080 already in use”:
> - Run netstat -ano | findstr :8080 to see which PID is using it.
> - Run taskkill /PID <PID> /F to stop that process.
> - Then run mvn spring-boot:run again.
6.4 Frontend (React + Vite)
From the frontend/ directory:
Install dependencies
cd frontendnpm install
Configure contract address
You can either:
Use .env:
VITE_CONTRACT_ADDRESS=0xYourDeployedAddress
and read it in contract.js, or
Put the address directly in src/config/contract.js.
Run in development mode
npm run dev
Frontend will run at: http://localhost:3000
7. Authentication & Authorization
7.1 User Entity & Profile Fields
The User entity in the backend includes:
Core auth
username (unique, required)
email (unique, required)
password (BCrypt hashed, never returned in API)
Profile
fullName
phoneNumber
country
createdAt / updatedAt
Relations
roles (many-to-many to Role)
wallet (one-to-one Wallet)
Sensitive fields like password and cyclic references (User.wallet / Wallet.user) are annotated so they are not serialized to JSON.
7.2 Signup Flow
Endpoint: POST /api/auth/signup
Request body (SignupRequest):
{  "username": "john_doe",  "email": "john@example.com",  "password": "Passw0rd!",  "fullName": "John Doe",  "phoneNumber": "+91-9876543210",  "country": "India"}
Validation:
username: 3–50 chars, required, unique.
email: valid email, required, unique.
password: at least 6 chars.
phoneNumber: optional, up to 20 chars.
country: optional, up to 100 chars.
Backend behavior:
Checks existsByUsername and existsByEmail.
Encrypts password with BCrypt.
Assigns default role ROLE_USER.
Saves user to users table.
Response:
200 OK with "User registered successfully" on success.
400 Bad Request with a readable message if username/email already exist.
7.3 Login Flow
Endpoint: POST /api/auth/login
Request body (LoginRequest):
{  "username": "john@example.com",   // can be username or email  "password": "Passw0rd!"}
Important behavior:
The backend treats the username field as “username or email”:
Tries findByUsername(value).
If that fails, tries findByEmail(value).
Uses AuthenticationManager with DaoAuthenticationProvider and UserDetailsServiceImpl.
On successful authentication:
Generates a JWT token via JwtService.
Returns a JwtResponse:
{  "token": "jwt-token-here",  "type": "Bearer",  "id": 1,  "username": "john_doe",  "email": "john@example.com",  "roles": ["ROLE_USER"],  "walletAddress": "0x1234..."   // null if not connected}
7.4 Frontend Auth Handling
AuthContext.jsx:
Stores user and token in React state and localStorage.
On page load, loads them from localStorage (if present).
Provides login(userData, token) and logout() helpers.
api.js:
Axios instance with base URL: http://localhost:8080/api.
Request interceptor:
Reads token from localStorage.
Adds Authorization: Bearer <token> header automatically.
Response interceptor:
On 401, clears auth and redirects to /login.
Logs additional info for 403 errors.
PrivateRoute.jsx:
Wraps routes that require authentication.
If no user in AuthContext, redirects to /login.
8. Database Model
8.1 Tables
users
id, username, email, password
full_name, phone_number, country
created_at, updated_at
roles
id, name (ROLE_USER, ROLE_CREATOR, ROLE_ADMIN)
user_roles
Join table between users and roles.
wallets
id, user_id (unique), address, connected_at, is_verified
campaigns
id, blockchain_id (contract campaignId)
creator_id
title, description, image_url, category
goal_amount, total_raised
deadline, goal_reached, funds_withdrawn
created_at, updated_at
donations
id
campaign_id, donor_id
transaction_hash (unique)
amount
donated_at, block_number
DataInitializer ensures default roles exist.
9. Smart Contract Design (Conceptual)
Key functions (simplified overview):
createCampaign(goal, deadline)
Creates a new campaign with:
goal in Wei,
deadline as a timestamp.
Returns a campaignId used off-chain as blockchain_id.
contribute(campaignId)
Payable function; user sends ETH.
Updates:
total raised for the campaign,
stores contributor balances (for refund eligibility).
withdrawFunds(campaignId)
Callable only by campaign owner.
Conditions:
goal reached,
deadline not passed or as business rules.
Transfers ETH from contract to creator’s wallet.
refund(campaignId)
Callable by donor.
Conditions:
deadline passed,
goal not met.
Transfers back the donor’s contribution.
View functions like:
getCampaign(campaignId)
isRefundable(campaignId)
getDonationCount(campaignId)
The backend maps these on-chain IDs to off-chain metadata via Campaign.blockchainId.
10. REST API Overview
Base URL: http://localhost:8080/api
10.1 Authentication
POST /auth/signup
Registers a new user with username, email, password, fullName, phoneNumber, country.
POST /auth/login
Authenticates using username OR email + password.
Returns JWT and user info.
10.2 Wallet
POST /wallet/connect
Body: { "address": "0x..." }
Requires valid JWT.
Binds ETH address to current user (one-to-one).
Prevents same address being used by multiple users.
GET /wallet/my-wallet
Returns:
"No wallet connected" string, or
WalletResponse JSON:
address
connectedAt
isVerified
GET /wallet/test-auth (debug)
Returns diagnostic data about the authentication context.
10.3 Campaigns
GET /campaigns
Public list of all campaigns (CampaignResponse DTO).
GET /campaigns/{id}
Details of specific campaign.
POST /campaigns/metadata (authenticated)
Used after contract createCampaign is already called from frontend.
Body: CampaignCreateRequest with metadata.
Creates a DB record linked to blockchainId.
GET /campaigns/my-campaigns (creator)
Returns campaigns created by current user.
POST /campaigns/{id}/sync
Backend pulls latest on-chain state (goal reached, total raised) via BlockchainService and updates the campaign.
10.4 Donations
POST /donations/record (authenticated)
Params: campaignId, transactionHash, amount, blockNumber
Records donation metadata for a transaction that already happened on-chain.
GET /donations/my-donations (authenticated)
Returns list of DonationResponse for the current user.
GET /donations/campaign/{campaignId}
Returns all donations for a campaign.
11. Frontend UX Flow
User visits http://localhost:3000
Automatically redirected to /dashboard if logged in,
If not logged in, PrivateRoute pushes them to /login.
Signup (/signup)
Fill: full name, username, email, password, phone number, country.
On success, redirected to /login.
Login (/login)
Fill: Username or Email + password.
On success:
JWT token and user object stored in localStorage and context.
Navigate to /dashboard.
Dashboard
Shows:
greeting with username,
whether wallet is connected,
recent campaigns.
Can click Connect MetaMask:
Frontend calls MetaMask to get address.
Then calls backend /wallet/connect to bind.
Campaign Pages
/campaigns – list all campaigns.
/campaigns/:id – details, progress bar, donate options.
/create-campaign – step to create new campaign.
My Data
/my-donations – list of personal donations.
/my-campaigns – list of campaigns created by user.
12. Role-Based Hybrid (ADMIN / USER)
- **User status**: ACTIVE or BLOCKED. Blocked users cannot login, create campaigns, or donate.
- **Campaign status**: ACTIVE, COMPLETED, or CANCELLED (DB + optional on-chain cancel).
- **Admin (ROLE_ADMIN)**:
  - GET /api/admin/users – list all users
  - PUT /api/admin/block/{id} – block user
  - PUT /api/admin/unblock/{id} – unblock user
  - GET /api/admin/campaigns – list all campaigns
  - DELETE /api/admin/campaign/{id} – mark campaign CANCELLED in DB
  - Admin cannot donate (enforced in backend and UI).
- **Contract owner**: The wallet that deployed the contract is the only one that can call `cancelCampaign(campaignId)` on-chain. Use the same wallet in MetaMask on Admin → Campaigns → "Cancel on chain" to cancel a campaign on the blockchain.
- **Creating first admin**: Assign role ROLE_ADMIN to a user in the `user_roles` and `roles` tables (e.g. insert into roles name='ROLE_ADMIN', then insert into user_roles user_id and role_id).

13. Fixes applied (data flow & stability)
- **Wallet API**: Reintroduced `WalletResponse` DTO; `GET /wallet/my-wallet` now returns `{ address, connectedAt, isVerified }` instead of the raw entity (avoids circular refs and exposes only needed fields).
- **Login**: Backend now supports login with **username or email** (UserDetailsServiceImpl tries `findByUsername` then `findByEmail`).
- **Frontend data**: Campaign list/detail/dashboard/my-campaigns use safe checks for API responses and progress (no division by zero / NaN when goal is 0).
- **Donations**: Donor wallet shown as "N/A" when not linked; frontend shows "Wallet not linked" when null.
- **Config**: Database password moved to env vars (`DB_USERNAME`, `DB_PASSWORD`); `allowPublicKeyRetrieval=true` added to JDBC URL for MySQL compatibility.
- **Cleanup**: Removed debug `System.out` from WalletController and GlobalExceptionHandler; simplified error responses.

14. Troubleshooting & Common Issues
Port 8080 already in use
Cause: another Java/Spring process running.
Fix:
netstat -ano | findstr :8080
tasklist /FI "PID eq <PID>"
taskkill /PID <PID> /F
Run mvn spring-boot:run again.
MySQL “Public Key Retrieval is not allowed”
Fix: ensure JDBC URL has allowPublicKeyRetrieval=true, e.g.:
    jdbc:mysql://localhost:3306/crowdfunding_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
CORS issues (403/401 with frontend)
Make sure:
Backend server.cors.allowed-origins includes http://localhost:3000.
Frontend is using http://localhost:8080/api as base URL.
JWT / Auth issues
Ensure JWT_SECRET in application.yml is:
at least 32 characters,
same across backend restarts if you want persistent tokens.
On frontend, if token is missing/expired, you will be redirected to /login.
This README now reflects:
The expanded signup fields (fullName, phoneNumber, country).
The login by username OR email behavior.
The current port configuration (8080).
The actual hybrid design currently implemented in your code.