package com.banking;

import java.util.List;
import java.util.Optional;

/**
 * Banking System Implementation
 * 
 * TODO: Implement all methods to pass the level-wise test cases.
 * 
 * INSTRUCTIONS:
 * 1. You can add any internal data structures you need (maps, lists, etc.)
 * 2. You can create helper methods and classes
 * 3. Make sure to handle all edge cases mentioned in the interface documentation
 * 4. Run tests level by level: mvn test -Dtest=Level1Test, etc.
 */
public class BankingSystemImpl implements BankingSystem {
    
    // TODO: Add your data structures here
    // Example:
    // private Map<String, Account> accounts = new HashMap<>();
    // private List<Transaction> transactions = new ArrayList<>();
    // private Map<String, ScheduledPayment> scheduledPayments = new HashMap<>();
    // private int paymentCounter = 0;
    
    public BankingSystemImpl() {
        // Initialize your data structures
    }
    
    // ========== LEVEL 1: Basic Operations ==========
    
    @Override
    public boolean createAccount(String accountId, int timestamp) {
        // TODO: Implement account creation
        // Return false if account already exists
        // Return true if account was successfully created
        throw new UnsupportedOperationException("createAccount not implemented yet");
    }
    
    @Override
    public Optional<Integer> deposit(String accountId, int timestamp, int amount) {
        // TODO: Implement deposit
        // Return empty Optional if account doesn't exist or amount is not positive
        // Return Optional with new balance if successful
        throw new UnsupportedOperationException("deposit not implemented yet");
    }
    
    @Override
    public Optional<Integer> transfer(String fromId, String toId, int timestamp, int amount) {
        // TODO: Implement transfer
        // Return empty Optional if:
        //   - Either account doesn't exist
        //   - Amount is not positive
        //   - Insufficient funds in source account
        // Return Optional with new balance of source account if successful
        throw new UnsupportedOperationException("transfer not implemented yet");
    }
    
    // ========== LEVEL 2: Ranking ==========
    
    @Override
    public List<String> topSpenders(int timestamp, int n) {
        // TODO: Implement top spenders ranking
        // Only count outgoing transfers (transfers FROM an account)
        // Sort by total outgoing amount (descending), then by account ID (ascending)
        // Return list in format: ["accountId(totalAmount)", ...]
        throw new UnsupportedOperationException("topSpenders not implemented yet");
    }
    
    // ========== LEVEL 3: Scheduled Payments ==========
    
    @Override
    public String schedulePayment(String accountId, String targetAccId, int timestamp, 
                                  int amount, double cashbackPercentage) {
        // TODO: Implement payment scheduling
        // Generate and return a unique payment ID
        // Store the payment for later processing
        throw new UnsupportedOperationException("schedulePayment not implemented yet");
    }
    
    @Override
    public String getPaymentStatus(String accountId, int timestamp, String paymentId) {
        // TODO: Implement payment status check
        // Return "SCHEDULED", "PROCESSED", "FAILED", or null
        throw new UnsupportedOperationException("getPaymentStatus not implemented yet");
    }
    
    @Override
    public void processScheduledPayments(int currentTimestamp) {
        // TODO: Implement scheduled payment processing
        // Process all payments with timestamp <= currentTimestamp
        // Apply cashback for successful payments
        // Mark failed payments (e.g., insufficient funds) as FAILED
        throw new UnsupportedOperationException("processScheduledPayments not implemented yet");
    }
    
    // ========== LEVEL 4: Account Merging ==========
    
    @Override
    public void mergeAccounts(String accountId1, String accountId2) {
        // TODO: Implement account merging
        // Combine balances into accountId1
        // Transfer all transaction history from accountId2 to accountId1
        // Remove/close accountId2
        throw new UnsupportedOperationException("mergeAccounts not implemented yet");
    }
}
