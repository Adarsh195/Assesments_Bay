package com.banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 2 Test Cases: Top Spenders Ranking
 * 
 * Tests for ranking accounts based on outgoing transactions
 */
@DisplayName("Level 2: Top Spenders Ranking")
class Level2Test {
    
    private BankingSystem bank;
    
    @BeforeEach
    void setUp() {
        bank = new BankingSystemImpl();
    }
    
    @Test
    @DisplayName("Should return empty list when no accounts exist")
    void testTopSpendersNoAccounts() {
        List<String> result = bank.topSpenders(5000, 3);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when no accounts exist");
    }
    
    @Test
    @DisplayName("Should return top spender with correct format")
    void testTopSpenderFormat() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        bank.transfer("acc1", "acc2", 1200, 500);
        
        List<String> result = bank.topSpenders(5000, 1);
        
        assertEquals(1, result.size(), "Should return 1 top spender");
        assertEquals("acc1(500)", result.get(0), "Format should be accountId(totalOutgoing)");
    }
    
    @Test
    @DisplayName("Should rank accounts by outgoing amount descending")
    void testTopSpendersDescending() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 2000);
        bank.deposit("acc2", 1100, 2000);
        bank.deposit("acc3", 1100, 2000);
        
        bank.transfer("acc1", "acc2", 1200, 1000); // acc1: 1000 outgoing
        bank.transfer("acc2", "acc3", 1300, 500);  // acc2: 500 outgoing
        bank.transfer("acc3", "acc1", 1400, 300);  // acc3: 300 outgoing
        
        List<String> result = bank.topSpenders(5000, 3);
        
        assertEquals(3, result.size());
        assertEquals("acc1(1000)", result.get(0), "acc1 should be #1");
        assertEquals("acc2(500)", result.get(1), "acc2 should be #2");
        assertEquals("acc3(300)", result.get(2), "acc3 should be #3");
    }
    
    @Test
    @DisplayName("Should break ties by account ID ascending")
    void testTopSpendersTieBreaker() {
        bank.createAccount("acc3", 1000);
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc4", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc2", 1100, 1000);
        bank.deposit("acc3", 1100, 1000);
        bank.deposit("acc4", 1100, 1000);
        
        // All have same outgoing amount (500)
        bank.transfer("acc1", "acc4", 1200, 500);
        bank.transfer("acc2", "acc4", 1300, 500);
        bank.transfer("acc3", "acc4", 1400, 500);
        
        List<String> result = bank.topSpenders(5000, 3);
        
        assertEquals(3, result.size());
        assertEquals("acc1(500)", result.get(0), "acc1 should be first (alphabetically)");
        assertEquals("acc2(500)", result.get(1), "acc2 should be second");
        assertEquals("acc3(500)", result.get(2), "acc3 should be third");
    }
    
    @Test
    @DisplayName("Should limit results to n accounts")
    void testTopSpendersLimit() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        bank.createAccount("acc4", 1000);
        bank.createAccount("acc5", 1000);
        
        bank.deposit("acc1", 1100, 5000);
        bank.deposit("acc2", 1100, 4000);
        bank.deposit("acc3", 1100, 3000);
        bank.deposit("acc4", 1100, 2000);
        bank.deposit("acc5", 1100, 1000);
        
        bank.transfer("acc1", "acc5", 1200, 500);
        bank.transfer("acc2", "acc5", 1200, 400);
        bank.transfer("acc3", "acc5", 1200, 300);
        bank.transfer("acc4", "acc5", 1200, 200);
        
        List<String> result = bank.topSpenders(5000, 2);
        
        assertEquals(2, result.size(), "Should return only top 2");
        assertEquals("acc1(500)", result.get(0));
        assertEquals("acc2(400)", result.get(1));
    }
    
    @Test
    @DisplayName("Should return all accounts when n exceeds account count")
    void testTopSpendersExceedsAccountCount() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        bank.transfer("acc1", "acc2", 1200, 300);
        
        List<String> result = bank.topSpenders(5000, 10);
        
        assertEquals(1, result.size(), "Should return only accounts with outgoing transactions");
        assertEquals("acc1(300)", result.get(0));
    }
    
    @Test
    @DisplayName("Should only count outgoing transfers, not incoming")
    void testTopSpendersOnlyOutgoing() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc3", 1100, 1000);
        
        // acc1 sends 500 to acc2
        bank.transfer("acc1", "acc2", 1200, 500);
        // acc3 sends 300 to acc2
        bank.transfer("acc3", "acc2", 1300, 300);
        
        // acc2 has received 800 but sent nothing
        List<String> result = bank.topSpenders(5000, 3);
        
        assertEquals(2, result.size(), "Only acc1 and acc3 should appear");
        assertEquals("acc1(500)", result.get(0));
        assertEquals("acc3(300)", result.get(1));
    }
    
    @Test
    @DisplayName("Should respect timestamp limit")
    void testTopSpendersTimestamp() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        
        bank.transfer("acc1", "acc2", 1200, 300); // timestamp 1200
        bank.transfer("acc1", "acc2", 1500, 200); // timestamp 1500
        
        List<String> result = bank.topSpenders(1300, 1);
        
        assertEquals(1, result.size());
        assertEquals("acc1(300)", result.get(0), "Should only count transaction at timestamp 1200");
    }
    
    @Test
    @DisplayName("Should accumulate multiple outgoing transactions")
    void testTopSpendersMultipleTransactions() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 5000);
        
        bank.transfer("acc1", "acc2", 1200, 1000);
        bank.transfer("acc1", "acc3", 1300, 500);
        bank.transfer("acc1", "acc2", 1400, 800);
        
        List<String> result = bank.topSpenders(5000, 1);
        
        assertEquals("acc1(2300)", result.get(0), "Should sum all outgoing: 1000+500+800=2300");
    }
    
    @Test
    @DisplayName("Should handle accounts with no outgoing transactions")
    void testTopSpendersNoOutgoing() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc2", 1100, 1000);
        bank.deposit("acc3", 1100, 1000);
        
        // Only deposits, no transfers
        List<String> result = bank.topSpenders(5000, 3);
        
        assertTrue(result.isEmpty(), "Should return empty list when no outgoing transactions");
    }
    
    @Test
    @DisplayName("Should handle complex ranking scenario")
    void testComplexRanking() {
        // Create 5 accounts with various transaction patterns
        bank.createAccount("charlie", 1000);
        bank.createAccount("alice", 1000);
        bank.createAccount("bob", 1000);
        bank.createAccount("david", 1000);
        bank.createAccount("eve", 1000);
        
        bank.deposit("alice", 1100, 10000);
        bank.deposit("bob", 1100, 10000);
        bank.deposit("charlie", 1100, 10000);
        bank.deposit("david", 1100, 10000);
        
        // alice: 5000 outgoing (2000 + 3000)
        bank.transfer("alice", "eve", 1200, 2000);
        bank.transfer("alice", "eve", 1300, 3000);
        
        // bob: 3000 outgoing
        bank.transfer("bob", "eve", 1400, 3000);
        
        // charlie: 5000 outgoing
        bank.transfer("charlie", "eve", 1500, 5000);
        
        // david: 3000 outgoing
        bank.transfer("david", "eve", 1600, 3000);
        
        List<String> result = bank.topSpenders(2000, 5);
        
        // Expected order:
        // 1. charlie(5000) - highest amount
        // 2. alice(5000) - tied with charlie, but alphabetically first
        // 3. bob(3000) - tied with david
        // 4. david(3000) - alphabetically after bob
        
        assertEquals(4, result.size());
        assertEquals("charlie(5000)", result.get(0));
        assertEquals("alice(5000)", result.get(1));
        assertEquals("bob(3000)", result.get(2));
        assertEquals("david(3000)", result.get(3));
    }
}
