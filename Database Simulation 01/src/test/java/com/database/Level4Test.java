package com.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 4 Test Cases: Time-Travel / Look-Back Operations
 * 
 * Tests for:
 * - Setting values at specific past timestamps
 * - Retrieving historical values
 * - Time-travel queries
 */
@DisplayName("Level 4: Time-Travel Operations")
class Level4Test {
    
    private InMemoryDatabase db;
    
    @BeforeEach
    void setUp() {
        db = new InMemoryDatabaseImpl();
    }
    
    // ========== Set At Timestamp Tests ==========
    
    @Test
    @DisplayName("Should set value at specific timestamp")
    void testSetAt() {
        db.setAt(500, "user", "name", "Alice", 100);
        
        String value = db.getAt(500, "user", "name", 100);
        assertEquals("Alice", value, "Should retrieve value at set timestamp");
    }
    
    @Test
    @DisplayName("Should retrieve value from past using getAt")
    void testGetAtPastTimestamp() {
        db.set(100, "user", "status", "active");
        db.set(200, "user", "status", "inactive");
        db.set(300, "user", "status", "suspended");
        
        // Query historical values
        assertEquals("active", db.getAt(400, "user", "status", 100));
        assertEquals("inactive", db.getAt(400, "user", "status", 200));
        assertEquals("suspended", db.getAt(400, "user", "status", 300));
    }
    
    @Test
    @DisplayName("Should return null for timestamp before first set")
    void testGetAtBeforeFirstSet() {
        db.set(200, "user", "name", "Alice");
        
        String value = db.getAt(500, "user", "name", 100);
        assertNull(value, "Should return null for timestamp before any set operation");
    }
    
    @Test
    @DisplayName("Should return latest value at or before query timestamp")
    void testGetAtLatestValueBefore() {
        db.set(100, "counter", "value", "10");
        db.set(200, "counter", "value", "20");
        db.set(300, "counter", "value", "30");
        
        // Query at timestamp 250 should return value from timestamp 200
        assertEquals("20", db.getAt(400, "counter", "value", 250));
        
        // Query at exact timestamp should return that value
        assertEquals("20", db.getAt(400, "counter", "value", 200));
        
        // Query before second set but after first
        assertEquals("10", db.getAt(400, "counter", "value", 150));
    }
    
    // ========== Set At With Historical Data Tests ==========
    
    @Test
    @DisplayName("Should insert historical data using setAt")
    void testSetAtHistorical() {
        // Insert historical data
        db.setAt(1000, "log", "event", "login", 100);
        db.setAt(1000, "log", "event", "action", 200);
        db.setAt(1000, "log", "event", "logout", 300);
        
        // Query different points in history
        assertEquals("login", db.getAt(1000, "log", "event", 100));
        assertEquals("action", db.getAt(1000, "log", "event", 200));
        assertEquals("logout", db.getAt(1000, "log", "event", 300));
    }
    
    @Test
    @DisplayName("Should handle mixed current and historical sets")
    void testMixedCurrentAndHistorical() {
        db.set(200, "user", "name", "Current");
        db.setAt(300, "user", "name", "Historical", 100);
        
        assertEquals("Historical", db.getAt(400, "user", "name", 100));
        assertEquals("Current", db.getAt(400, "user", "name", 200));
    }
    
    @Test
    @DisplayName("Should handle setAt with future timestamp relative to setTimestamp")
    void testSetAtFutureRelative() {
        // Set current time to 500, but record as if set at 100
        db.setAt(500, "user", "field", "value", 100);
        
        // Should be queryable at timestamp 100
        assertEquals("value", db.getAt(600, "user", "field", 100));
    }
    
    // ========== Time-Travel With Updates Tests ==========
    
    @Test
    @DisplayName("Should track value changes over time")
    void testValueChangesOverTime() {
        db.set(100, "stock", "price", "100");
        db.set(200, "stock", "price", "105");
        db.set(300, "stock", "price", "102");
        db.set(400, "stock", "price", "110");
        
        // Query at different points in time
        assertEquals("100", db.getAt(500, "stock", "price", 150));
        assertEquals("105", db.getAt(500, "stock", "price", 250));
        assertEquals("102", db.getAt(500, "stock", "price", 350));
        assertEquals("110", db.getAt(500, "stock", "price", 450));
    }
    
    @Test
    @DisplayName("Should handle getAt after field deletion")
    void testGetAtAfterDeletion() {
        db.set(100, "user", "temp", "value1");
        db.set(200, "user", "temp", "value2");
        db.compareAndDelete(300, "user", "temp", "value2");
        
        // Historical queries before deletion should still work
        assertEquals("value1", db.getAt(400, "user", "temp", 100));
        assertEquals("value2", db.getAt(400, "user", "temp", 200));
        
        // Query at deletion time or after should return null
        assertNull(db.getAt(400, "user", "temp", 300));
        assertNull(db.getAt(400, "user", "temp", 350));
    }
    
    @Test
    @DisplayName("Should handle recreation after deletion in time-travel")
    void testRecreationAfterDeletion() {
        db.set(100, "user", "field", "value1");
        db.compareAndDelete(200, "user", "field", "value1");
        db.set(300, "user", "field", "value2");
        
        assertEquals("value1", db.getAt(400, "user", "field", 150));
        assertNull(db.getAt(400, "user", "field", 250));
        assertEquals("value2", db.getAt(400, "user", "field", 350));
    }
    
    // ========== Time-Travel With TTL Tests ==========
    
    @Test
    @DisplayName("Should handle getAt with expired TTL fields")
    void testGetAtWithExpiredTTL() {
        db.setWithTTL(100, "session", "token", "abc123", 50); // Expires at 150
        
        // Query before expiration
        assertEquals("abc123", db.getAt(200, "session", "token", 120));
        
        // Query at expiration or after - debatable behavior
        // Option 1: Return null (field is expired)
        // Option 2: Return value (historical query ignores TTL)
        // We'll test for null (respecting TTL even in historical queries)
        assertNull(db.getAt(200, "session", "token", 150));
    }
    
    @Test
    @DisplayName("Should handle TTL in historical context")
    void testTTLInHistoricalContext() {
        db.setWithTTL(100, "cache", "key", "value1", 100); // Expires at 200
        db.set(250, "cache", "key", "value2"); // Regular set after expiration
        
        assertEquals("value1", db.getAt(300, "cache", "key", 150)); // Before expiry
        assertNull(db.getAt(300, "cache", "key", 220)); // After expiry, before new set
        assertEquals("value2", db.getAt(300, "cache", "key", 270)); // After new set
    }
    
    // ========== Complex Time-Travel Scenarios ==========
    
    @Test
    @DisplayName("Should handle complex time-travel scenario with multiple fields")
    void testComplexTimeTravelScenario() {
        // Simulate user profile changes over time
        db.set(100, "user1", "name", "Alice");
        db.set(100, "user1", "age", "25");
        
        db.set(200, "user1", "age", "26"); // Birthday
        
        db.set(300, "user1", "name", "Alice Smith"); // Married
        db.set(300, "user1", "city", "NYC"); // New field
        
        // Query at time 150
        assertEquals("Alice", db.getAt(400, "user1", "name", 150));
        assertEquals("25", db.getAt(400, "user1", "age", 150));
        assertNull(db.getAt(400, "user1", "city", 150));
        
        // Query at time 250
        assertEquals("Alice", db.getAt(400, "user1", "name", 250));
        assertEquals("26", db.getAt(400, "user1", "age", 250));
        assertNull(db.getAt(400, "user1", "city", 250));
        
        // Query at time 350
        assertEquals("Alice Smith", db.getAt(400, "user1", "name", 350));
        assertEquals("26", db.getAt(400, "user1", "age", 350));
        assertEquals("NYC", db.getAt(400, "user1", "city", 350));
    }
    
    @Test
    @DisplayName("Should handle setAt inserting into middle of history")
    void testSetAtMiddleOfHistory() {
        db.set(100, "value", "field", "v1");
        db.set(300, "value", "field", "v3");
        
        // Insert a value at timestamp 200 (between 100 and 300)
        db.setAt(400, "value", "field", "v2", 200);
        
        assertEquals("v1", db.getAt(500, "value", "field", 150));
        assertEquals("v2", db.getAt(500, "value", "field", 250));
        assertEquals("v3", db.getAt(500, "value", "field", 350));
    }
    
    @Test
    @DisplayName("Should handle multiple setAt operations at same timestamp")
    void testMultipleSetAtSameTimestamp() {
        db.setAt(500, "log", "event", "first", 100);
        db.setAt(500, "log", "event", "second", 100);
        
        // Latest setAt should win
        assertEquals("second", db.getAt(600, "log", "event", 100));
    }
    
    @Test
    @DisplayName("Should handle time-travel across multiple keys")
    void testTimeTravelMultipleKeys() {
        db.set(100, "user1", "status", "online");
        db.set(100, "user2", "status", "offline");
        
        db.set(200, "user1", "status", "away");
        
        // Query both users at time 150
        assertEquals("online", db.getAt(300, "user1", "status", 150));
        assertEquals("offline", db.getAt(300, "user2", "status", 150));
        
        // Query both users at time 250
        assertEquals("away", db.getAt(300, "user1", "status", 250));
        assertEquals("offline", db.getAt(300, "user2", "status", 250));
    }
    
    @Test
    @DisplayName("Should return current value when querying at current timestamp")
    void testGetAtCurrentTimestamp() {
        db.set(100, "user", "field", "value1");
        db.set(200, "user", "field", "value2");
        
        // get() and getAt() at current timestamp should return same value
        String getCurrentValue = db.get(300, "user", "field");
        String getAtCurrentValue = db.getAt(300, "user", "field", 300);
        
        assertEquals(getCurrentValue, getAtCurrentValue);
    }
    
    @Test
    @DisplayName("Should handle very long history")
    void testLongHistory() {
        // Create a long history of changes
        for (int i = 0; i < 100; i++) {
            db.set(i * 100, "counter", "value", String.valueOf(i));
        }
        
        // Query various points
        assertEquals("0", db.getAt(10000, "counter", "value", 50));
        assertEquals("5", db.getAt(10000, "counter", "value", 550));
        assertEquals("10", db.getAt(10000, "counter", "value", 1050));
        assertEquals("50", db.getAt(10000, "counter", "value", 5050));
        assertEquals("99", db.getAt(10000, "counter", "value", 9950));
    }
}
