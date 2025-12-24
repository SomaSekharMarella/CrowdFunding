const hre = require("hardhat");

async function main() {
  console.log("Deploying Crowdfunding contract...");
  
  const Crowdfunding = await hre.ethers.getContractFactory("Crowdfunding");
  const crowdfunding = await Crowdfunding.deploy();
  
  await crowdfunding.waitForDeployment();
  
  const address = await crowdfunding.getAddress();
  console.log("Crowdfunding contract deployed to:", address);
  console.log("Network:", hre.network.name);
  
  // Save deployment info
  console.log("\nDeployment Info:");
  console.log("Contract Address:", address);
  console.log("Network:", hre.network.name);
  console.log("\nAdd this to your backend .env file:");
  console.log(`CROWDFUNDING_CONTRACT_ADDRESS=${address}`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
