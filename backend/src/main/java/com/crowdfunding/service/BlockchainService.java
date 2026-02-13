package com.crowdfunding.service;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Blockchain Service - Read-only operations
 * NO private keys, NO transaction signing
 */
@Service
public class BlockchainService {
    
    private final Web3j web3j;
    private final String contractAddress;
    
    public BlockchainService(
            @Value("${blockchain.network-url}") String networkUrl,
            @Value("${blockchain.contract-address}") String contractAddress) {
        this.web3j = Web3j.build(new HttpService(networkUrl));
        this.contractAddress = contractAddress;
    }
    
    /**
     * Get campaign details from blockchain.
     *
     * NOTE: To stay compatible with older deployed contracts that returned only
     * 6 values (without `active`) and newer ones that return 7, we decode only
     * the first 6 values and treat `active` as `true` by default. This avoids
     * ABI length / range errors like "Range [...] out of bounds for length ...".
     */
    public CampaignData getCampaign(Long campaignId) throws Exception {
        Function function = new Function(
            "getCampaign",
            Arrays.asList(new Uint256(BigInteger.valueOf(campaignId))),
            Arrays.asList(
                new TypeReference<org.web3j.abi.datatypes.Address>() {},
                new TypeReference<Uint256>() {},
                new TypeReference<Uint256>() {},
                new TypeReference<Uint256>() {},
                new TypeReference<org.web3j.abi.datatypes.Bool>() {},
                new TypeReference<org.web3j.abi.datatypes.Bool>() {}
            )
        );
        
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        ).send();
        
        List<Type> decoded = FunctionReturnDecoder.decode(
            response.getValue(),
            function.getOutputParameters()
        );
        
        // We don't decode the optional `active` flag here to remain compatible
        // with contracts that don't return it. Assume active=true by default.
        Boolean activeValue = true;
        return new CampaignData(
            decoded.get(0).getValue().toString(), // creator
            (BigInteger) decoded.get(1).getValue(), // goal
            (BigInteger) decoded.get(2).getValue(), // deadline
            (BigInteger) decoded.get(3).getValue(), // totalRaised
            (Boolean) decoded.get(4).getValue(),   // goalReached
            (Boolean) decoded.get(5).getValue(),  // fundsWithdrawn
            activeValue // active
        );
    }
    
    /**
     * Get total campaign count
     */
    public Long getCampaignCount() throws Exception {
        Function function = new Function(
            "getCampaignCount",
            Collections.emptyList(),
            Arrays.asList(new TypeReference<Uint256>() {})
        );
        
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        ).send();
        
        List<Type> decoded = FunctionReturnDecoder.decode(
            response.getValue(),
            function.getOutputParameters()
        );
        
        return ((BigInteger) decoded.get(0).getValue()).longValue();
    }
    
    /**
     * Get donation count for a campaign
     */
    public Long getDonationCount(Long campaignId) throws Exception {
        Function function = new Function(
            "getDonationCount",
            Arrays.asList(new Uint256(BigInteger.valueOf(campaignId))),
            Arrays.asList(new TypeReference<Uint256>() {})
        );
        
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        ).send();
        
        List<Type> decoded = FunctionReturnDecoder.decode(
            response.getValue(),
            function.getOutputParameters()
        );
        
        return ((BigInteger) decoded.get(0).getValue()).longValue();
    }
    
    /**
     * Check if campaign is refundable
     */
    public Boolean isRefundable(Long campaignId) throws Exception {
        Function function = new Function(
            "isRefundable",
            Arrays.asList(new Uint256(BigInteger.valueOf(campaignId))),
            Arrays.asList(new TypeReference<org.web3j.abi.datatypes.Bool>() {})
        );
        
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        ).send();
        
        List<Type> decoded = FunctionReturnDecoder.decode(
            response.getValue(),
            function.getOutputParameters()
        );
        
        return (Boolean) decoded.get(0).getValue();
    }
    
    /**
     * Campaign data from blockchain
     */
    public static class CampaignData {
        public final String owner; // creator address
        public final BigInteger goal;
        public final BigInteger deadline;
        public final BigInteger totalRaised;
        public final Boolean goalReached;
        public final Boolean fundsWithdrawn;
        public final Boolean active;

        public CampaignData(String owner, BigInteger goal, BigInteger deadline,
                            BigInteger totalRaised, Boolean goalReached, Boolean fundsWithdrawn, Boolean active) {
            this.owner = owner;
            this.goal = goal;
            this.deadline = deadline;
            this.totalRaised = totalRaised;
            this.goalReached = goalReached;
            this.fundsWithdrawn = fundsWithdrawn;
            this.active = active != null ? active : true;
        }
    }
}
