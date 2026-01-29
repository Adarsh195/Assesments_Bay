package com.banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 3 Test Cases: Scheduled Payments with Cashback
 * 
 * Tests for:
 * - Scheduling payments
 * - Checking payment status
 * - Processing scheduled payments
 * - Cashback application
 */
@DisplayName("Level 3: Scheduled Payments")
class Level3Test {
    
    private BankingSystem bank;
    
    @BeforeEach
    void setUp() {
        bank = new BankingSystemImpl();
    }
    
    @Test
    @DisplayName("Should schedule payment and return payment ID")
    void testSchedulePayment() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 5.0);
        
        assertNotNull(paymentId, "Payment ID should not be null");
        assertFalse(paymentId.isEmpty(), "Payment ID should not be empty");
    }
    
    @Test
    @DisplayName("Should return unique payment IDs")
    void testUniquePaymentIds() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        String paymentId1 = bank.schedulePayment("acc1", "acc2", 2000, 500, 5.0);
        String paymentId2 = bank.schedulePayment("acc1", "acc2", 2100, 300, 3.0);
        
        assertNotEquals(paymentId1, paymentId2, "Payment IDs should be unique");
    }
    
    @Test
    @DisplayName("Should show payment as SCHEDULED before processing")
    void testPaymentStatusScheduled() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        
        String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 5.0);
        String status = bank.getPaymentStatus("acc1", 1500, paymentId);
        
        assertEquals("SCHEDULED", status, "Payment should be SCHEDULED before processing time");
    }
    
    @Test
    @DisplayName("Should return null for non-existent payment ID")
    void testPaymentStatusNonExistent() {
        bank.createAccount("acc1", 1000);
        
        String status = bank.getPaymentStatus("acc1", 2000, "nonexistent");
        
        assertNull(status, "Should return null for non-existent payment ID");
    }
    
    @Test
    @DisplayName("Should process scheduled payment successfully")
    void testProcessScheduledPayment() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        
        String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 0.0);
        bank.processScheduledPayments(2000);
        
        String status = bank.getPaymentStatus("acc1", 2100, paymentId);
        assertEquals("PROCESSED", status, "Payment should be PROCESSED after processing");
    }
    
    @Test
    @DisplayName("Should apply cashback after successful payment")
    void testCashbackApplication() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        
        // Schedule payment with 10% cashback
        bank.schedulePayment("acc1", "acc2", 2000, 500, 10.0);
        bank.processScheduledPayments(2000);
        
        // acc1 should have: 1000 - 500 + 50 (cashback) = 550
        // Verify by depositing 1 and checking balance
        int balance = bank.deposit("acc1", 2100, 1).get();
        assertEquals(551, balance, "acc1 should have 551 after payment and cashback");
    }
    
    @Test
    @DisplayName("Should mark payment as FAILED with insufficient funds")
    void testPaymentFailedInsufficientFunds() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 100); // Only 100 available
        
        String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 0.0);
        bank.processScheduledPayments(2000);
        
        String status = bank.getPaymentStatus("acc1", 2100, paymentId);
        assertEquals("FAILED", status, "Payment should be FAILED with insufficient funds");
    }
    
    @Test
    @DisplayName("Should not process payment before scheduled time")
    void testPaymentNotProcessedEarly() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        
        String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 0.0);
        bank.processScheduledPayments(1500); // Before scheduled time
        
        String status = bank.getPaymentStatus("acc1", 1600, paymentId);
        assertEquals("SCHEDULED", status, "Payment should still be SCHEDULED");
    }
    
    @Test
    @DisplayName("Should process payment exactly at scheduled time")
    void testPaymentProcessedAtExactTime() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        
        String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 0.0);
        bank.processScheduledPayments(2000); // Exact scheduled time
        
        String status = bank.getPaymentStatus("acc1", 2100, paymentId);
        assertEquals("PROCESSED", status, "Payment should be PROCESSED at exact scheduled time");
    }
    
    @Test
    @DisplayName("Should process multiple scheduled payments")
    void testMultipleScheduledPayments() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        bank.deposit("acc1", 1100, 2000);
        
        String paymentId1 = bank.schedulePayment("acc1", "acc2", 2000, 500, 0.0);
        String paymentId2 = bank.schedulePayment("acc1", "acc3", 2000, 300, 0.0);
        
        bank.processScheduledPayments(2000);
        
        assertEquals("PROCESSED", bank.getPaymentStatus("acc1", 2100, paymentId1));
        assertEquals("PROCESSED", bank.getPaymentStatus("acc1", 2100, paymentId2));
        
        // acc1 should have: 2000 - 500 - 300 = 1200
        int balance = bank.deposit("acc1", 2100, 0).get();
        assertEquals(1200, balance);
    }
    
    @Test
    @DisplayName("Should process payments scheduled at different times")
    void testPaymentsAtDifferentTimes() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 2000);
        
        String paymentId1 = bank.schedulePayment("acc1", "acc2", 2000, 500, 0.0);
        String paymentId2 = bank.schedulePayment("acc1", "acc2", 2500, 300, 0.0);
        
        bank.processScheduledPayments(2200); // Process first payment only
        
        assertEquals("PROCESSED", bank.getPaymentStatus("acc1", 2300, paymentId1));
        assertEquals("SCHEDULED", bank.getPaymentStatus("acc1", 2300, paymentId2));
    }
    
    @Test
    @DisplayName("Should handle cashback calculation correctly")
    void testCashbackCalculation() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        
        // 20% cashback on 500 = 100
        bank.schedulePayment("acc1", "acc2", 2000, 500, 20.0);
        bank.processScheduledPayments(2000);
        
        // acc1: 1000 - 500 + 100 = 600
        int balance = bank.deposit("acc1", 2100, 0).get();
        assertEquals(600, balance, "Balance should include cashback");
    }
    
    @Test
    @DisplayName("Should update both accounts after payment processing")
    void testBothAccountsUpdated() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 1000);
        bank.deposit("acc2", 1100, 500);
        
        bank.schedulePayment("acc1", "acc2", 2000, 300, 10.0);
        bank.processScheduledPayments(2000);
        
        // acc1: 1000 - 300 + 30 (cashback) = 730
        // acc2: 500 + 300 = 800
        int balance1 = bank.deposit("acc1", 2100, 0).get();
        int balance2 = bank.deposit("acc2", 2100, 0).get();
        
        assertEquals(730, balance1);
        assertEquals(800, balance2);
    }
    
    @Test
    @DisplayName("Should not apply cashback for failed payments")
    void testNoCashbackForFailedPayments() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.deposit("acc1", 1100, 100); // Insufficient for payment
        
        bank.schedulePayment("acc1", "acc2", 2000, 500, 50.0); // 50% cashback
        bank.processScheduledPayments(2000);
        
        // acc1 should still have 100 (no change)
        int balance = bank.deposit("acc1", 2100, 0).get();
        assertEquals(100, balance, "Balance should not change for failed payment");
    }
    
    @Test
    @DisplayName("Should handle complex scheduled payment scenario")
    void testComplexScheduledPayments() {
        bank.createAccount("acc1", 1000);
        bank.createAccount("acc2", 1000);
        bank.createAccount("acc3", 1000);
        
        bank.deposit("acc1", 1100, 5000);
        bank.deposit("acc2", 1100, 3000);
        
        // Schedule multiple payments from different accounts
        String p1 = bank.schedulePayment("acc1", "acc3", 2000, 1000, 5.0);  // 50 cashback
        String p2 = bank.schedulePayment("acc2", "acc3", 2000, 500, 10.0);   // 50 cashback
        String p3 = bank.schedulePayment("acc1", "acc2", 2500, 2000, 2.5);   // 50 cashback
        
        // Process first batch
        bank.processScheduledPayments(2000);
        
        assertEquals("PROCESSED", bank.getPaymentStatus("acc1", 2100, p1));
        assertEquals("PROCESSED", bank.getPaymentStatus("acc2", 2100, p2));
        assertEquals("SCHEDULED", bank.getPaymentStatus("acc1", 2100, p3));
        
        // acc1: 5000 - 1000 + 50 = 4050
        // acc2: 3000 - 500 + 50 = 2550
        // acc3: 0 + 1000 + 500 = 1500
        
        assertEquals(4050, bank.deposit("acc1", 2100, 0).get());
        assertEquals(2550, bank.deposit("acc2", 2100, 0).get());
        assertEquals(1500, bank.deposit("acc3", 2100, 0).get());
        
        // Process second batch
        bank.processScheduledPayments(2500);
        
        assertEquals("PROCESSED", bank.getPaymentStatus("acc1", 2600, p3));
        
        // acc1: 4050 - 2000 + 50 = 2100
        // acc2: 2550 + 2000 = 4550
        
        assertEquals(2100, bank.deposit("acc1", 2600, 0).get());
        assertEquals(4550, bank.deposit("acc2", 2600, 0).get());
    }
}
