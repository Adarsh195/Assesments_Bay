package com.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 2 Test Cases: Scanning and Filtering Operations
 * 
 * Tests for:
 * - Scanning all fields in a record
 * - Prefix-based filtering
 * - Sorting requirements
 */
@DisplayName("Level 2: Scanning Operations")
class Level2Test {
    
    private InMemoryDatabase db;
    
    @BeforeEach
    void setUp() {
        db = new InMemoryDatabaseImpl();
    }
    
    // ========== Scan Tests ==========
    
    @Test
    @DisplayName("Should scan all fields in a record")
    void testScanBasic() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        db.set(300, "user1", "city", "NYC");
        
        List<String> result = db.scan(400, "user1");
        
        assertEquals(3, result.size(), "Should return 3 fields");
        assertTrue(result.contains("name(Alice)"));
        assertTrue(result.contains("age(30)"));
        assertTrue(result.contains("city(NYC)"));
    }
    
    @Test
    @DisplayName("Should return fields sorted by field name")
    void testScanSorted() {
        db.set(100, "user1", "zebra", "z");
        db.set(200, "user1", "apple", "a");
        db.set(300, "user1", "mango", "m");
        
        List<String> result = db.scan(400, "user1");
        
        assertEquals(3, result.size());
        assertEquals("apple(a)", result.get(0), "First should be apple");
        assertEquals("mango(m)", result.get(1), "Second should be mango");
        assertEquals("zebra(z)", result.get(2), "Third should be zebra");
    }
    
    @Test
    @DisplayName("Should return empty list for non-existent key")
    void testScanNonExistentKey() {
        List<String> result = db.scan(100, "nonexistent");
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Should return empty list");
    }
    
    @Test
    @DisplayName("Should return empty list for key with no fields")
    void testScanEmptyRecord() {
        db.set(100, "user1", "name", "Alice");
        db.compareAndDelete(200, "user1", "name", "Alice");
        
        List<String> result = db.scan(300, "user1");
        
        assertTrue(result.isEmpty(), "Should return empty list after all fields deleted");
    }
    
    @Test
    @DisplayName("Should scan only existing fields")
    void testScanAfterPartialDelete() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        db.set(300, "user1", "city", "NYC");
        
        db.compareAndDelete(400, "user1", "age", "30");
        
        List<String> result = db.scan(500, "user1");
        
        assertEquals(2, result.size());
        assertTrue(result.contains("name(Alice)"));
        assertTrue(result.contains("city(NYC)"));
        assertFalse(result.contains("age(30)"));
    }
    
    @Test
    @DisplayName("Should reflect latest values in scan")
    void testScanWithUpdates() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        
        db.set(300, "user1", "name", "Alicia"); // Update
        
        List<String> result = db.scan(400, "user1");
        
        assertEquals(2, result.size());
        assertTrue(result.contains("name(Alicia)"), "Should show updated value");
        assertTrue(result.contains("age(30)"));
    }
    
    // ========== Scan By Prefix Tests ==========
    
    @Test
    @DisplayName("Should scan fields matching prefix")
    void testScanByPrefixBasic() {
        db.set(100, "user1", "user_name", "Alice");
        db.set(200, "user1", "user_age", "30");
        db.set(300, "user1", "admin_level", "5");
        db.set(400, "user1", "city", "NYC");
        
        List<String> result = db.scanByPrefix(500, "user1", "user_");
        
        assertEquals(2, result.size());
        assertTrue(result.contains("user_name(Alice)"));
        assertTrue(result.contains("user_age(30)"));
        assertFalse(result.contains("admin_level(5)"));
        assertFalse(result.contains("city(NYC)"));
    }
    
    @Test
    @DisplayName("Should return empty list when no fields match prefix")
    void testScanByPrefixNoMatch() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        
        List<String> result = db.scanByPrefix(300, "user1", "admin_");
        
        assertTrue(result.isEmpty(), "Should return empty list when no matches");
    }
    
    @Test
    @DisplayName("Should return fields sorted by field name for prefix scan")
    void testScanByPrefixSorted() {
        db.set(100, "user1", "user_zebra", "z");
        db.set(200, "user1", "user_apple", "a");
        db.set(300, "user1", "user_mango", "m");
        db.set(400, "user1", "admin_test", "t");
        
        List<String> result = db.scanByPrefix(500, "user1", "user_");
        
        assertEquals(3, result.size());
        assertEquals("user_apple(a)", result.get(0));
        assertEquals("user_mango(m)", result.get(1));
        assertEquals("user_zebra(z)", result.get(2));
    }
    
    @Test
    @DisplayName("Should scan with empty prefix returns all fields")
    void testScanByEmptyPrefix() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        
        List<String> result = db.scanByPrefix(300, "user1", "");
        
        assertEquals(2, result.size(), "Empty prefix should match all fields");
    }
    
    @Test
    @DisplayName("Should handle prefix that matches single character")
    void testScanBySingleCharPrefix() {
        db.set(100, "user1", "alpha", "1");
        db.set(200, "user1", "apple", "2");
        db.set(300, "user1", "banana", "3");
        db.set(400, "user1", "avocado", "4");
        
        List<String> result = db.scanByPrefix(500, "user1", "a");
        
        assertEquals(3, result.size());
        assertTrue(result.contains("alpha(1)"));
        assertTrue(result.contains("apple(2)"));
        assertTrue(result.contains("avocado(4)"));
    }
    
    @Test
    @DisplayName("Should return empty for prefix scan on non-existent key")
    void testScanByPrefixNonExistentKey() {
        List<String> result = db.scanByPrefix(100, "nonexistent", "user_");
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle exact field name as prefix")
    void testScanByExactFieldName() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "name_display", "Alicia");
        
        List<String> result = db.scanByPrefix(300, "user1", "name");
        
        assertEquals(2, result.size());
        assertTrue(result.contains("name(Alice)"));
        assertTrue(result.contains("name_display(Alicia)"));
    }
    
    // ========== Complex Scanning Tests ==========
    
    @Test
    @DisplayName("Should handle multiple scans with updates")
    void testMultipleScansWithUpdates() {
        db.set(100, "user1", "field1", "value1");
        db.set(200, "user1", "field2", "value2");
        
        List<String> scan1 = db.scan(300, "user1");
        assertEquals(2, scan1.size());
        
        db.set(400, "user1", "field3", "value3");
        
        List<String> scan2 = db.scan(500, "user1");
        assertEquals(3, scan2.size());
        
        db.compareAndDelete(600, "user1", "field2", "value2");
        
        List<String> scan3 = db.scan(700, "user1");
        assertEquals(2, scan3.size());
        assertTrue(scan3.contains("field1(value1)"));
        assertTrue(scan3.contains("field3(value3)"));
    }
    
    @Test
    @DisplayName("Should handle scan across multiple keys")
    void testScanMultipleKeys() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        db.set(300, "user2", "name", "Bob");
        db.set(400, "user2", "city", "LA");
        
        List<String> user1Scan = db.scan(500, "user1");
        List<String> user2Scan = db.scan(500, "user2");
        
        assertEquals(2, user1Scan.size());
        assertEquals(2, user2Scan.size());
        assertTrue(user1Scan.contains("name(Alice)"));
        assertTrue(user2Scan.contains("name(Bob)"));
    }
    
    @Test
    @DisplayName("Should handle complex prefix patterns")
    void testComplexPrefixPatterns() {
        db.set(100, "config", "app.server.host", "localhost");
        db.set(200, "config", "app.server.port", "8080");
        db.set(300, "config", "app.db.host", "dbhost");
        db.set(400, "config", "app.db.port", "5432");
        db.set(500, "config", "logging.level", "INFO");
        
        List<String> appSettings = db.scanByPrefix(600, "config", "app.");
        assertEquals(4, appSettings.size());
        
        List<String> serverSettings = db.scanByPrefix(600, "config", "app.server.");
        assertEquals(2, serverSettings.size());
        assertTrue(serverSettings.contains("app.server.host(localhost)"));
        assertTrue(serverSettings.contains("app.server.port(8080)"));
    }
}
