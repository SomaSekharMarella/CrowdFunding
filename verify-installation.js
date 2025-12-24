#!/usr/bin/env node

/**
 * Installation Verification Script
 * Checks if all dependencies are installed correctly
 */

const fs = require('fs');
const path = require('path');

console.log('🔍 Verifying Installation...\n');

let allGood = true;

// Check blockchain
console.log('📦 Checking Blockchain...');
const blockchainPackageJson = path.join(__dirname, 'blockchain', 'package.json');
const blockchainNodeModules = path.join(__dirname, 'blockchain', 'node_modules');

if (fs.existsSync(blockchainPackageJson)) {
  if (fs.existsSync(blockchainNodeModules)) {
    console.log('   ✅ Blockchain dependencies installed');
  } else {
    console.log('   ❌ Blockchain node_modules not found. Run: cd blockchain && npm install');
    allGood = false;
  }
} else {
  console.log('   ⚠️  Blockchain package.json not found');
}

// Check frontend
console.log('\n📦 Checking Frontend...');
const frontendPackageJson = path.join(__dirname, 'frontend', 'package.json');
const frontendNodeModules = path.join(__dirname, 'frontend', 'node_modules');

if (fs.existsSync(frontendPackageJson)) {
  if (fs.existsSync(frontendNodeModules)) {
    console.log('   ✅ Frontend dependencies installed');
  } else {
    console.log('   ❌ Frontend node_modules not found. Run: cd frontend && npm install');
    allGood = false;
  }
} else {
  console.log('   ⚠️  Frontend package.json not found');
}

// Check backend
console.log('\n📦 Checking Backend...');
const backendPom = path.join(__dirname, 'backend', 'pom.xml');
const backendTarget = path.join(__dirname, 'backend', 'target');

if (fs.existsSync(backendPom)) {
  console.log('   ✅ Backend pom.xml found');
  if (fs.existsSync(backendTarget)) {
    console.log('   ✅ Backend compiled (target directory exists)');
  } else {
    console.log('   ⚠️  Backend not compiled yet. Run: cd backend && mvn clean install');
  }
} else {
  console.log('   ⚠️  Backend pom.xml not found');
}

// Check configuration files
console.log('\n⚙️  Checking Configuration...');

const blockchainEnv = path.join(__dirname, 'blockchain', '.env');
const frontendEnv = path.join(__dirname, 'frontend', '.env');
const backendYml = path.join(__dirname, 'backend', 'src', 'main', 'resources', 'application.yml');

if (fs.existsSync(blockchainEnv)) {
  console.log('   ✅ Blockchain .env file exists');
} else {
  console.log('   ⚠️  Blockchain .env not found. Create it with SEPOLIA_RPC_URL and PRIVATE_KEY');
}

if (fs.existsSync(frontendEnv)) {
  console.log('   ✅ Frontend .env file exists');
} else {
  console.log('   ⚠️  Frontend .env not found. Create it with VITE_CONTRACT_ADDRESS');
}

if (fs.existsSync(backendYml)) {
  console.log('   ✅ Backend application.yml exists');
} else {
  console.log('   ❌ Backend application.yml not found');
  allGood = false;
}

console.log('\n' + '='.repeat(50));

if (allGood) {
  console.log('✅ Installation looks good!');
  console.log('\n📝 Next Steps:');
  console.log('   1. Configure .env files (blockchain and frontend)');
  console.log('   2. Update backend application.yml with database and blockchain config');
  console.log('   3. Deploy smart contract: cd blockchain && npm run deploy:sepolia');
  console.log('   4. Start backend: cd backend && mvn spring-boot:run');
  console.log('   5. Start frontend: cd frontend && npm run dev');
} else {
  console.log('❌ Some issues found. Please fix them before proceeding.');
}

console.log('\n📚 See SETUP.md for detailed instructions\n');
