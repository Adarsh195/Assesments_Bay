package com.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 3 Test Cases: TTL (Time-To-Live) Operations
 * 
 * Tests for:
 * - Setting values with TTL
 * - Automatic expiration
 * - Compare-and-set with TTL
 */
@DisplayName("Level 3: TTL Operations")
class Level3Test {
    
    private InMemoryDatabase db;
    
    @BeforeEach
    void setUp() {
        db = new InMemoryDatabaseImpl();
    }
    
    // ========== Set With TTL Tests ==========
    
    @Test
    @DisplayName("Should set value with TTL")
    void testSetWithTTL() {
        db.setWithTTL(100, "session", "token", "abc123", 50);
        
        String value = db.get(120, "session", "token");
        assertEquals("abc123", value, "Value should be accessible before expiration");
    }
    
    @Test
    @DisplayName("Should expire field after TTL")
    void testExpireAfterTTL() {
        db.setWithTTL(100, "session", "token", "abc123", 50);
        // TTL = 50, so expires at timestamp 150
        
        assertEquals("abc123", db.get(149, "session", "token"), "Should exist just before expiration");
        assertNull(db.get(150, "session", "token"), "Should be null at expiration time");
    assertNull(db.get(200, "session", "token"), "Should be null after expiration");
    }
    
    @Test
    @DisplayName("Should not include expired fields in scan")
    void testExpiredNotInScan() {
        db.setWithTTL(100, "user", "temp_field", "temporary", 30);
        db.set(100, "user", "permanent_field", "permanent");
        
        // Before expiration
        List<String> scan1 = db.scan(120, "user");
        assertEquals(2, scan1.size());
        assertTrue(scan1.contains("temp_field(temporary)"));
        
        // After expiration (100 + 30 = 130)
        List<String> scan2 = db.scan(130, "user");
        assertEquals(1, scan2.size());
        assertTrue(scan2.contains("permanent_field(permanent)"));
        assertFalse(scan2.contains("temp_field(temporary)"));
    }
    
    @Test
    @DisplayName("Should handle TTL of zero (immediate expiration)")
    void testTTLZero() {
        db.setWithTTL(100, "user", "field", "value", 0);
        
        assertNull(db.get(100, "user", "field"), "Should expire immediately");
    }
    
    @Test
    @DisplayName("Should update TTL when setting same field again")
    void testUpdateTTL() {
        db.setWithTTL(100, "session", "token", "abc123", 50); // Expires at 150
        db.setWithTTL(120, "session", "token", "xyz789", 100); // Expires at 220
        
        assertNull(db.get(150, "session", "token"), "Old TTL should not apply");
        assertEquals("xyz789", db.get(200, "session", "token"), "New value should exist");
        assertNull(db.get(220, "session", "token"), "Should expire at new TTL time");
    }
    
    @Test
    @DisplayName("Should handle multiple fields with different TTLs")
    void testMultipleTTLs() {
        db.setWithTTL(100, "cache", "key1", "value1", 50);  // Expires at 150
        db.setWithTTL(100, "cache", "key2", "value2", 100); // Expires at 200
        db.setWithTTL(100, "cache", "key3", "value3", 150); // Expires at 250
        
        // At time 175: key1 expired, key2 and key3 still valid
        List<String> scan = db.scan(175, "cache");
        assertEquals(2, scan.size());
        assertTrue(scan.contains("key2(value2)"));
        assertTrue(scan.contains("key3(value3)"));
    }
    
    // ========== Compare-and-Set With TTL Tests ==========
    
    @Test
    @DisplayName("Should compare-and-set with TTL on matching value")
    void testCompareAndSetWithTTLSuccess() {
        db.set(100, "user", "status", "active");
        boolean result = db.compareAndSetWithTTL(200, "user", "status", "active", "inactive", 50);
        
        assertTrue(result, "Should succeed");
        assertEquals("inactive", db.get(230, "user", "status"));
        assertNull(db.get(250, "user", "status"), "Should expire after TTL");
    }
    
    @Test
    @DisplayName("Should not compare-and-set with TTL on non-matching value")
    void testCompareAndSetWithTTLFailure() {
        db.set(100, "user", "status", "active");
        boolean result = db.compareAndSetWithTTL(200, "user", "status", "inactive", "pending", 50);
        
        assertFalse(result, "Should fail");
        assertEquals("active", db.get(300, "user", "status"), "Value should not change");
    }
    
    @Test
    @DisplayName("Should compare-and-set on non-existent field with null")
    void testCompareAndSetWithTTLNullExpected() {
        boolean result = db.compareAndSetWithTTL(100, "user", "field", null, "value", 50);
        
        assertTrue(result, "Should succeed with null expected value");
        assertEquals("value", db.get(120, "user", "field"));
        assertNull(db.get(150, "user", "field"), "Should expire");
    }
    
    @Test
    @DisplayName("Should not compare-and-set on expired field")
    void testCompareAndSetWithTTLOnExpired() {
        db.setWithTTL(100, "user", "field", "old", 30); // Expires at 130
        
        // Try to compare-and-set after expiration
        boolean result = db.compareAndSetWithTTL(150, "user", "field", "old", "new", 50);
        
        assertFalse(result, "Should fail because field is expired");
    }
    
    @Test
    @DisplayName("Should replace TTL when compare-and-set succeeds")
    void testCompareAndSetReplaceTTL() {
        db.setWithTTL(100, "cache", "key", "value1", 100); // Expires at 200
        
        db.compareAndSetWithTTL(150, "cache", "key", "value1", "value2", 200); // New expiry at 350
        
        assertEquals("value2", db.get(200, "cache", "key"), "Should still exist at old expiry time");
        assertEquals("value2", db.get(340, "cache", "key"), "Should exist before new expiry");
        assertNull(db.get(350, "cache", "key"), "Should expire at new TTL time");
    }
    
    // ========== Mixed TTL and Regular Operations Tests ==========
    
    @Test
    @DisplayName("Should override TTL when using regular set")
    void testRegularSetOverridesTTL() {
        db.setWithTTL(100, "user", "field", "temp", 50); // Expires at 150
        db.set(120, "user", "field", "permanent");
        
        assertEquals("permanent", db.get(200, "user", "field"), "Should not expire");
    }
    
    @Test
    @DisplayName("Should set TTL when using setWithTTL on regular field")
    void testSetTTLOnRegularField() {
        db.set(100, "user", "field", "permanent");
        db.setWithTTL(150, "user", "field", "temporary", 50); // Expires at 200
        
        assertEquals("temporary", db.get(180, "user", "field"));
        assertNull(db.get(200, "user", "field"), "Should expire");
    }
    
    @Test
    @DisplayName("Should handle compareAndDelete on field with TTL")
    void testCompareAndDeleteWithTTL() {
        db.setWithTTL(100, "user", "field", "value", 100); // Expires at 200
        
        boolean result = db.compareAndDelete(150, "user", "field", "value");
        
        assertTrue(result, "Should delete successfully");
        assertNull(db.get(160, "user", "field"), "Should be deleted before expiration");
    }
    
    @Test
    @DisplayName("Should handle regular compareAndSet on field with TTL")
    void testRegularCompareAndSetOnTTLField() {
        db.setWithTTL(100, "user", "field", "value1", 100); // Expires at 200
        
        boolean result = db.compareAndSet(150, "user", "field", "value1", "value2");
        
        assertTrue(result, "Should succeed");
        assertEquals("value2", db.get(250, "user", "field"), "TTL should be removed");
    }
    
    // ========== Complex TTL Scenarios ==========
    
    @Test
    @DisplayName("Should handle complex TTL scenario")
    void testComplexTTLScenario() {
        // Set multiple fields with different TTLs
        db.setWithTTL(100, "session", "id", "sess123", 200);      // Expires at 300
        db.setWithTTL(100, "session", "user", "alice", 150);      // Expires at 250
        db.set(100, "session", "created", "100");                  // No TTL
        
        // At time 200: all fields exist
        List<String> scan1 = db.scan(200, "session");
        assertEquals(3, scan1.size());
        
        // At time 250: user expired
        List<String> scan2 = db.scan(250, "session");
        assertEquals(2, scan2.size());
        assertTrue(scan2.contains("id(sess123)"));
        assertTrue(scan2.contains("created(100)"));
        
        // At time 300: id expired
        List<String> scan3 = db.scan(300, "session");
        assertEquals(1, scan3.size());
        assertTrue(scan3.contains("created(100)"));
    }
    
    @Test
    @DisplayName("Should handle scanByPrefix with expired fields")
    void testScanByPrefixWithExpiration() {
        db.setWithTTL(100, "user", "tmp_field1", "value1", 50);
        db.setWithTTL(100, "user", "tmp_field2", "value2", 100);
        db.set(100, "user", "tmp_field3", "value3");
        
        // Before first expiration
        List<String> scan1 = db.scanByPrefix(120, "user", "tmp_");
        assertEquals(3, scan1.size());
        
        // After first expiration (100 + 50 = 150)
        List<String> scan2 = db.scanByPrefix(150, "user", "tmp_");
        assertEquals(2, scan2.size());
        assertTrue(scan2.contains("tmp_field2(value2)"));
        assertTrue(scan2.contains("tmp_field3(value3)"));
    }
    
    @Test
    @DisplayName("Should handle long TTL values")
    void testLongTTL() {
        db.setWithTTL(100, "archive", "data", "old_data", 1000000);
        
        assertEquals("old_data", db.get(50000, "archive", "data"));
        assertEquals("old_data", db.get(1000000, "archive", "data"));
        assertNull(db.get(1000101, "archive", "data"));
    }
}
