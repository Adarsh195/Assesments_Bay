package com.banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 1 Test Cases: Basic Banking Operations
 * 
 * Tests for:
 * - Account creation
 * - Deposit operations
 * - Transfer operations
 */
@DisplayName("Level 1: Basic Banking Operations")
class Level1Test {
    
    private BankingSystem bank;
    
    @BeforeEach
    void setUp() {
        bank = new BankingSystemImpl();
    }
    
    // ========== Account Creation Tests ==========
    
    @Test
    @DisplayName("Should create account successfully")
    void testCreateAccount() {
        boolean result = bank.createAccount("acc1", 1000);
        assertTrue(result, "Account should be created successfully");
    }
    
    @Test
    @DisplayName("Should not create duplicate account")
    void testCreateDuplicateAccount() {
        bank.createAccount("acc1", 1000);
        boolean result = bank.createAccount("acc1", 2000);
        assertFalse(result, "Duplicate account should not be created");
    }
    
    @Test
    @DisplayName("Should create multiple different accounts")
    void testCreateMultipleAccounts() {
        assertTrue(bank.createAccount("acc1", 1000));
        assertTrue(bank.createAccount("acc2", 1100));
        assertTrue(bank.createAccount("acc3", 1200));
    }
    
    // ========== Deposit Tests ==========
    
    @Test
    @DisplayName("Should deposit money successfully")
    void testDeposit() {
        bank.createAccount("acc1", 1000);
        Optional<Integer> balance = bank.deposit("acc1", 1100, 500);
        
        assertTrue(balance.isPresent(), "Deposit should succeed");
        assertEquals(500, balance.get(), "Balance should be 500 after deposit");
    }
    
    @Test
    @DisplayName("Should handle multiple deposits")
    void testMultipleDeposits() {
        bank.createAccount("acc1", 1000);
        bank.deposit("acc1", 1100, 500);
        Optional<Integer> balance = bank.deposit("acc1", 1200, 300);
        
        assertTrue(balance.isPresent());
        assertEquals(800, balance.get(), "Balance should be 800 after two deposits");
    }
    
    @Test
    @DisplayName("Should reject deposit to non-existent account")
    void testDepositToNonExistentAccount() {
        Optional<Integer> balance = bank.deposit("nonexistent", 1000, 500);
        assertFalse(balance.isPresent(), "Deposit to non-existent account should fail");
    }
    
    @Test
    @DisplayName("Should reject deposit with zero amount")
    void testDepositZeroAmount() {
        bank.createAccount("acc1", 1000);
        Optional<Integer> balance = bank.deposit("acc1", 1100, 0);
        assertFalse(balance.isPresent(), "Deposit with zero amount should fail");
    }
    
    @Test
    @DisplayName("Should reject deposit with negative amount")
    void testDepositNegativeAmount() {
        bank.createAccount("acc1", 1000);
        Optional<Integer> balance = bank.deposit("acc1", 1100, -100);
        assertFalse(balance.isPresent(), "Deposit with negative amount should fail");
    }
    
    // ========== Transfer Tests ==========
    
    @Test
    @DisplayName("Should transfer money successfully")
    void testTransfer() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        
        Optional<Integer> balance = bank.transfer("acc1", "acc2", 1200, 300);
        
        assertTrue(balance.isPresent(), "Transfer should succeed");
        assertEquals(700, balance.get(), "Source account should have 700 after transfer");
    }
    
    @Test
    @DisplayName("Should update both accounts after transfer")
    void testTransferUpdatesAccounts() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc2", 1100, 500);
        
        bank.transfer("acc1", "acc2", 1200, 300);
        
        // Verify by depositing 0 to check balance (creative way to check)
        Optional<Integer> balance1 = bank.deposit("acc1", 1300, 1);
        Optional<Integer> balance2 = bank.deposit("acc2", 1300, 1);
        
        assertEquals(701, balance1.get(), "Source account should have 701");
        assertEquals(801, balance2.get(), "Destination account should have 801");
    }
    
    @Test
    @DisplayName("Should reject transfer with insufficient funds")
    void testTransferInsufficientFunds() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 100);
        
        Optional<Integer> balance = bank.transfer("acc1", "acc2", 1200, 300);
        
        assertFalse(balance.isPresent(), "Transfer with insufficient funds should fail");
    }
    
    @Test
    @DisplayName("Should reject transfer from non-existent account")
    void testTransferFromNonExistentAccount() {
        bank.createAccount("acc2", 1000);
        Optional<Integer> balance = bank.transfer("nonexistent", "acc2", 1200, 100);
        assertFalse(balance.isPresent(), "Transfer from non-existent account should fail");
    }
    
    @Test
    @DisplayName("Should reject transfer to non-existent account")
    void testTransferToNonExistentAccount() {
        bank.createAccount("acc1", 1000);
        bank.deposit("acc1", 1100, 500);
        Optional<Integer> balance = bank.transfer("acc1", "nonexistent", 1200, 100);
        assertFalse(balance.isPresent(), "Transfer to non-existent account should fail");
    }
    
    @Test
    @DisplayName("Should reject transfer with zero amount")
    void testTransferZeroAmount() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 500);
        
        Optional<Integer> balance = bank.transfer("acc1", "acc2", 1200, 0);
        assertFalse(balance.isPresent(), "Transfer with zero amount should fail");
    }
    
    @Test
    @DisplayName("Should reject transfer with negative amount")
    void testTransferNegativeAmount() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 500);
        
        Optional<Integer> balance = bank.transfer("acc1", "acc2", 1200, -100);
        assertFalse(balance.isPresent(), "Transfer with negative amount should fail");
    }
    
    @Test
    @DisplayName("Should allow transfer of exact balance")
    void testTransferExactBalance() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 500);
        
        Optional<Integer> balance = bank.transfer("acc1", "acc2", 1200, 500);
        
        assertTrue(balance.isPresent(), "Transfer of exact balance should succeed");
        assertEquals(0, balance.get(), "Source account should have 0 after transferring all funds");
    }
    
    // ========== Complex Scenario Tests ==========
    
    @Test
    @DisplayName("Should handle complex transaction sequence")
    void testComplexTransactionSequence() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc2", 1200, 500);
        
        bank.transfer("acc1", "acc2", 1300, 300);
        bank.transfer("acc2", "acc3", 1400, 200);
        bank.transfer("acc1", "acc3", 1500, 100);
        
        // Verify final balances
        Optional<Integer> bal1 = bank.deposit("acc1", 1600, 0);
        Optional<Integer> bal2 = bank.deposit("acc2", 1600, 0);
        Optional<Integer> bal3 = bank.deposit("acc3", 1600, 0);
        
        // acc1: 1000 - 300 - 100 = 600
        // acc2: 500 + 300 - 200 = 600
        // acc3: 0 + 200 + 100 = 300
        assertEquals(600, bal1.get(), "acc1 final balance should be 600");
        assertEquals(600, bal2.get(), "acc2 final balance should be 600");
        assertEquals(300, bal3.get(), "acc3 final balance should be 300");
    }
}
