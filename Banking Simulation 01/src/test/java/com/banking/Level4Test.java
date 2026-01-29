package com.banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 4 Test Cases: Account Merging
 * 
 * Tests for merging accounts, combining balances, and updating transaction histories
 */
@DisplayName("Level 4: Account Merging")
class Level4Test {
    
    private BankingSystem bank;
    
    @BeforeEach
    void setUp() {
        bank = new BankingSystemImpl();
    }
    
    @Test
    @DisplayName("Should merge two accounts with combined balance")
    void testMergeAccountsBasic() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        bank.deposit("acc1", 1100, 500);
        bank.deposit("acc2", 1100, 300);
        
        bank.mergeAccounts("acc1", "acc2");
        
        // acc1 should have 500 + 300 = 800
        int balance = bank.deposit("acc1", 1200, 0).get();
        assertEquals(800, balance, "acc1 should have combined balance");
    }
    
    @Test
    @DisplayName("Should close merged account")
    void testMergedAccountClosed() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        bank.deposit("acc1", 1100, 500);
        bank.deposit("acc2", 1100, 300);
        
        bank.mergeAccounts("acc1", "acc2");
        
        // acc2 should no longer exist
        assertFalse(bank.deposit("acc2", 1200, 100).isPresent(), 
                   "acc2 should not exist after merge");
    }
    
    @Test
    @DisplayName("Should transfer transaction history from merged account")
    void testMergeTransactionHistory() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc2", 1100, 1000);
        bank.deposit("acc3", 1100, 500);
        
        // acc2 sends money to acc3
        bank.transfer("acc2", "acc3", 1200, 400);
        
        // Before merge, acc2 has spent 400
        List<String> beforeMerge = bank.topSpenders(1300, 5);
        assertTrue(beforeMerge.contains("acc2(400)"), "acc2 should be in top spenders");
        
        // Merge acc2 into acc1
        bank.mergeAccounts("acc1", "acc2");
        
        // After merge, acc1 should have acc2's spending history
        List<String> afterMerge = bank.topSpenders(1400, 5);
        assertTrue(afterMerge.contains("acc1(400)"), 
                  "acc1 should have acc2's transaction history");
        assertFalse(afterMerge.contains("acc2(400)"), 
                   "acc2 should not appear in top spenders");
    }
    
    @Test
    @DisplayName("Should merge accounts with zero balance")
    void testMergeZeroBalance() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        bank.deposit("acc1", 1100, 500);
        // acc2 has 0 balance
        
        bank.mergeAccounts("acc1", "acc2");
        
        int balance = bank.deposit("acc1", 1200, 0).get();
        assertEquals(500, balance, "acc1 should retain its balance");
    }
    
    @Test
    @DisplayName("Should accumulate spending from both accounts")
    void testMergeAccumulateSpending() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 2000);
        bank.deposit("acc2", 1100, 2000);
        
        bank.transfer("acc1", "acc3", 1200, 500);  // acc1 spent 500
        bank.transfer("acc2", "acc3", 1300, 300);  // acc2 spent 300
        
        bank.mergeAccounts("acc1", "acc2");
        
        // acc1 should now have spending of 500 + 300 = 800
        List<String> result = bank.topSpenders(1400, 1);
        assertEquals("acc1(800)", result.get(0), 
                    "acc1 should have combined spending");
    }
    
    @Test
    @DisplayName("Should handle merging account with scheduled payments")
    void testMergeWithScheduledPayments() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 2000);
        bank.deposit("acc2", 1100, 1000);
        
        // Schedule payment from acc2
        String paymentId = bank.schedulePayment("acc2", "acc3", 2000, 500, 10.0);
        
        // Merge acc2 into acc1
        bank.mergeAccounts("acc1", "acc2");
        
        // Process scheduled payments
        bank.processScheduledPayments(2000);
        
        // The payment should still be processed (now from acc1)
        // acc1: 2000 + 1000 - 500 + 50 (cashback) = 2550
        int balance = bank.deposit("acc1", 2100, 0).get();
        assertEquals(2550, balance, 
                    "acc1 should process acc2's scheduled payment");
    }
    
    @Test
    @DisplayName("Should update all references to merged account")
    void testMergeUpdatesAllReferences() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 2000);
        bank.deposit("acc2", 1100, 2000);
        bank.deposit("acc3", 1100, 2000);
        
        // acc3 transfers to acc2
        bank.transfer("acc3", "acc2", 1200, 500);
        
        // Merge acc2 into acc1
        bank.mergeAccounts("acc1", "acc2");
        
        // acc3 should still show as having spent 500
        List<String> result = bank.topSpenders(1300, 5);
        assertTrue(result.contains("acc3(500)"), 
                  "acc3 spending should still be tracked");
        
        // acc1 should have received the 500 that was sent to acc2
        // acc1: 2000 + 2000 + 500 = 4500
        int balance = bank.deposit("acc1", 1300, 0).get();
        assertEquals(4500, balance);
    }
    
    @Test
    @DisplayName("Should handle multiple sequential merges")
    void testMultipleSequentialMerges() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 100);
        bank.deposit("acc2", 1100, 200);
        bank.deposit("acc3", 1100, 300);
        
        // Merge acc2 into acc1: acc1 = 100 + 200 = 300
        bank.mergeAccounts("acc1", "acc2");
        assertEquals(300, bank.deposit("acc1", 1200, 0).get());
        
        // Merge acc3 into acc1: acc1 = 300 + 300 = 600
        bank.mergeAccounts("acc1", "acc3");
        assertEquals(600, bank.deposit("acc1", 1300, 0).get());
        
        // Only acc1 should exist
        assertFalse(bank.deposit("acc2", 1400, 0).isPresent());
        assertFalse(bank.deposit("acc3", 1400, 0).isPresent());
    }
    
    @Test
    @DisplayName("Should combine complex transaction histories")
    void testComplexTransactionHistoryMerge() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        bank.createAccount("acc4", 1000);
        
        bank.deposit("acc1", 1100, 5000);
        bank.deposit("acc2", 1100, 5000);
        bank.deposit("acc3", 1100, 1000);
        bank.deposit("acc4", 1100, 1000);
        
        // acc1 transactions
        bank.transfer("acc1", "acc3", 1200, 1000);
        bank.transfer("acc1", "acc4", 1300, 500);
        
        // acc2 transactions
        bank.transfer("acc2", "acc3", 1400, 800);
        bank.transfer("acc2", "acc4", 1500, 600);
        
        // Before merge
        List<String> before = bank.topSpenders(1600, 5);
        assertTrue(before.contains("acc1(1500)"), "acc1 spent 1500");
        assertTrue(before.contains("acc2(1400)"), "acc2 spent 1400");
        
        // Merge acc2 into acc1
        bank.mergeAccounts("acc1", "acc2");
        
        // After merge, acc1 should have combined spending: 1500 + 1400 = 2900
        List<String> after = bank.topSpenders(1700, 5);
        assertTrue(after.contains("acc1(2900)"), 
                  "acc1 should have combined spending of 2900");
        assertFalse(after.stream().anyMatch(s -> s.startsWith("acc2(")), 
                   "acc2 should not appear in results");
        
        // Combined balance: 5000 - 1500 + 5000 - 1400 = 7100
        assertEquals(7100, bank.deposit("acc1", 1800, 0).get());
    }
    
    @Test
    @DisplayName("Should allow creating new account with merged account ID")
    void testReuseAccountIdAfterMerge() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        bank.deposit("acc1", 1100, 500);
        bank.deposit("acc2", 1100, 300);
        
        bank.mergeAccounts("acc1", "acc2");
        
        // Should be able to create a new acc2
        boolean created = bank.createAccount("acc2", 1500);
        assertTrue(created, "Should be able to reuse merged account ID");
        
        // New acc2 should have 0 balance
        int balance = bank.deposit("acc2", 1600, 100).get();
        assertEquals(100, balance, "New acc2 should start fresh");
    }
    
    @Test
    @DisplayName("Should handle merge with scheduled payments from both accounts")
    void testMergeBothAccountsHaveScheduledPayments() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 3000);
        bank.deposit("acc2", 1100, 3000);
        
        // Schedule payments from both accounts
        String p1 = bank.schedulePayment("acc1", "acc3", 2000, 500, 10.0);
        String p2 = bank.schedulePayment("acc2", "acc3", 2000, 300, 5.0);
        
        // Merge acc2 into acc1
        bank.mergeAccounts("acc1", "acc2");
        
        // Process payments
        bank.processScheduledPayments(2000);
        
        // Both payments should be processed
        // acc1: 3000 + 3000 - 500 - 300 + 50 + 15 = 5265
        int balance = bank.deposit("acc1", 2100, 0).get();
        assertEquals(5265, balance);
        
        // acc3 should have received: 500 + 300 = 800
        assertEquals(800, bank.deposit("acc3", 2100, 0).get());
    }
    
    @Test
    @DisplayName("Should handle edge case: merge account with itself (should be no-op or handle gracefully)")
    void testMergeAccountWithItself() {
        bank.createAccount("acc1", 1000);
        bank.deposit("acc1", 1100, 500);
        
        // This is an edge case - implementation can choose to:
        // 1. Do nothing (no-op)
        // 2. Throw exception
        // 3. Handle gracefully
        // For this test, we'll just ensure it doesn't corrupt data
        try {
            bank.mergeAccounts("acc1", "acc1");
        } catch (Exception e) {
            // Exception is acceptable for this edge case
        }
        
        // acc1 should still exist with original balance
        int balance = bank.deposit("acc1", 1200, 0).get();
        assertEquals(500, balance, "Original balance should be preserved");
    }
}
