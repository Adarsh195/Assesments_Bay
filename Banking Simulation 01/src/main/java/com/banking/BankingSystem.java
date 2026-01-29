package com.banking;

import java.util.List;
import java.util.Optional;

/**
 * Banking System Interface for Interview Coding Assessment
 * 
 * This interface defines all operations required across 4 difficulty levels:
 * - Level 1: Basic account operations (create, deposit, transfer)
 * - Level 2: Account ranking (top spenders)
 * - Level 3: Scheduled payments with cashback
 * - Level 4: Account merging
 */
public interface BankingSystem {
    
    // ========== LEVEL 1: Basic Operations ==========
    
    /**
     * Creates a new account with the given account ID at the specified timestamp.
     * 
     * @param accountId The unique identifier for the account
     * @param timestamp The timestamp when the account is created
     * @return true if the account was successfully created, false if the account already exists
     */
    boolean createAccount(String accountId, int timestamp);
    
    /**
     * Deposits money into the specified account.
     * 
     * @param accountId The account to deposit into
     * @param timestamp The timestamp of the deposit
     * @param amount The amount to deposit (must be positive)
     * @return Optional containing the new balance if successful, empty Optional if:
     *         - Account doesn't exist
     *         - Amount is not positive
     */
    Optional<Integer> deposit(String accountId, int timestamp, int amount);
    
    /**
     * Transfers money from one account to another.
     * 
     * @param fromId The source account
     * @param toId The destination account
     * @param timestamp The timestamp of the transfer
     * @param amount The amount to transfer (must be positive)
     * @return Optional containing the new balance of the source account if successful,
     *         empty Optional if:
     *         - Either account doesn't exist
     *         - Amount is not positive
     *         - Insufficient funds in source account
     */
    Optional<Integer> transfer(String fromId, String toId, int timestamp, int amount);
    
    // ========== LEVEL 2: Ranking ==========
    
    /**
     * Returns the top N accounts based on total outgoing transactions up to the given timestamp.
     * Outgoing transactions include transfers FROM an account (not TO an account).
     * 
     * Results should be sorted by:
     * 1. Total outgoing amount (descending)
     * 2. Account ID (ascending) for ties
     * 
     * @param timestamp Consider only transactions up to this timestamp
     * @param n The number of top spenders to return
     * @return List of account IDs in the format "accountId(totalOutgoing)"
     *         Example: ["acc1(5000)", "acc2(3000)", "acc3(3000)"]
     */
    List<String> topSpenders(int timestamp, int n);
    
    // ========== LEVEL 3: Scheduled Payments ==========
    
    /**
     * Schedules a payment to be processed at a future timestamp.
     * The payment will be executed when processScheduledPayments() is called with a timestamp
     * >= the scheduled timestamp.
     * 
     * @param accountId The source account for the payment
     * @param targetAccId The destination account for the payment
     * @param timestamp The timestamp when the payment should be processed
     * @param amount The amount to transfer
     * @param cashbackPercentage The cashback percentage (0-100) to be credited back to source account
     * @return A unique payment ID for tracking the payment status
     */
    String schedulePayment(String accountId, String targetAccId, int timestamp, 
                          int amount, double cashbackPercentage);
    
    /**
     * Gets the status of a scheduled payment.
     * 
     * @param accountId The account that scheduled the payment
     * @param timestamp The current timestamp
     * @param paymentId The payment ID returned by schedulePayment()
     * @return One of:
     *         - "SCHEDULED" if payment hasn't been processed yet
     *         - "PROCESSED" if payment was successfully executed
     *         - "FAILED" if payment processing failed (e.g., insufficient funds)
     *         - null if payment ID doesn't exist for this account
     */
    String getPaymentStatus(String accountId, int timestamp, String paymentId);
    
    /**
     * Processes all scheduled payments that are due at or before the current timestamp.
     * For each successful payment:
     * 1. Transfer the amount from source to destination
     * 2. Apply cashback to the source account
     * 
     * Payments that fail (e.g., insufficient funds) should be marked as FAILED.
     * 
     * @param currentTimestamp Process all payments scheduled at or before this timestamp
     */
    void processScheduledPayments(int currentTimestamp);
    
    // ========== LEVEL 4: Account Merging ==========
    
    /**
     * Merges two accounts into one.
     * After merging:
     * 1. accountId1 remains active with combined balance of both accounts
     * 2. accountId2 is closed/removed
     * 3. All transactions from accountId2 should be attributed to accountId1
     * 4. All future references to accountId2 should be invalid
     * 
     * @param accountId1 The account to keep (will have combined balance)
     * @param accountId2 The account to merge into accountId1 (will be closed)
     */
    void mergeAccounts(String accountId1, String accountId2);
}
