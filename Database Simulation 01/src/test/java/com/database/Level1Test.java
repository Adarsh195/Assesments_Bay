package com.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 1 Test Cases: Basic Database Operations
 * 
 * Tests for:
 * - Set and Get operations
 * - Compare-and-set atomic operations
 * - Compare-and-delete operations
 */
@DisplayName("Level 1: Basic Operations")
class Level1Test {
    
    private InMemoryDatabase db;
    
    @BeforeEach
    void setUp() {
        db = new InMemoryDatabaseImpl();
    }
    
    // ========== Set and Get Tests ==========
    
    @Test
    @DisplayName("Should set and get a value")
    void testSetAndGet() {
        db.set(100, "user1", "name", "Alice");
        String value = db.get(200, "user1", "name");
        
        assertEquals("Alice", value, "Should retrieve the set value");
    }
    
    @Test
    @DisplayName("Should set multiple fields for same key")
    void testSetMultipleFields() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        db.set(300, "user1", "city", "NYC");
        
        assertEquals("Alice", db.get(400, "user1", "name"));
        assertEquals("30", db.get(400, "user1", "age"));
        assertEquals("NYC", db.get(400, "user1", "city"));
    }
    
    @Test
    @DisplayName("Should set fields for multiple keys")
    void testSetMultipleKeys() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user2", "name", "Bob");
        db.set(300, "user3", "name", "Charlie");
        
        assertEquals("Alice", db.get(400, "user1", "name"));
        assertEquals("Bob", db.get(400, "user2", "name"));
        assertEquals("Charlie", db.get(400, "user3", "name"));
    }
    
    @Test
    @DisplayName("Should update existing value")
    void testUpdateValue() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "name", "Alicia");
        
        String value = db.get(300, "user1", "name");
        assertEquals("Alicia", value, "Should retrieve the updated value");
    }
    
    @Test
    @DisplayName("Should return null for non-existent key")
    void testGetNonExistentKey() {
        String value = db.get(100, "nonexistent", "name");
        assertNull(value, "Should return null for non-existent key");
    }
    
    @Test
    @DisplayName("Should return null for non-existent field")
    void testGetNonExistentField() {
        db.set(100, "user1", "name", "Alice");
        String value = db.get(200, "user1", "age");
        
        assertNull(value, "Should return null for non-existent field");
    }
    
    @Test
    @DisplayName("Should handle empty string value")
    void testEmptyStringValue() {
        db.set(100, "user1", "description", "");
        String value = db.get(200, "user1", "description");
        
        assertEquals("", value, "Should handle empty string values");
    }
    
    // ========== Compare-and-Set Tests ==========
    
    @Test
    @DisplayName("Should compare-and-set with matching value")
    void testCompareAndSetSuccess() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndSet(200, "user1", "name", "Alice", "Alicia");
        
        assertTrue(result, "compareAndSet should return true");
        assertEquals("Alicia", db.get(300, "user1", "name"), "Value should be updated");
    }
    
    @Test
    @DisplayName("Should not compare-and-set with non-matching value")
    void testCompareAndSetFailure() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndSet(200, "user1", "name", "Bob", "Charlie");
        
        assertFalse(result, "compareAndSet should return false");
        assertEquals("Alice", db.get(300, "user1", "name"), "Value should remain unchanged");
    }
    
    @Test
    @DisplayName("Should fail compare-and-set for non-existent key")
    void testCompareAndSetNonExistentKey() {
        boolean result = db.compareAndSet(100, "nonexistent", "name", "Alice", "Bob");
        
        assertFalse(result, "compareAndSet should fail for non-existent key");
    }
    
    @Test
    @DisplayName("Should fail compare-and-set for non-existent field")
    void testCompareAndSetNonExistentField() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndSet(200, "user1", "age", "30", "31");
        
        assertFalse(result, "compareAndSet should fail for non-existent field");
    }
    
    @Test
    @DisplayName("Should compare-and-set with null expected value for non-existent field")
    void testCompareAndSetNullExpected() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndSet(200, "user1", "age", null, "30");
        
        assertTrue(result, "compareAndSet should succeed with null for non-existent field");
        assertEquals("30", db.get(300, "user1", "age"), "Field should be created");
    }
    
    // ========== Compare-and-Delete Tests ==========
    
    @Test
    @DisplayName("Should compare-and-delete with matching value")
    void testCompareAndDeleteSuccess() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndDelete(200, "user1", "name", "Alice");
        
        assertTrue(result, "compareAndDelete should return true");
        assertNull(db.get(300, "user1", "name"), "Field should be deleted");
    }
    
    @Test
    @DisplayName("Should not compare-and-delete with non-matching value")
    void testCompareAndDeleteFailure() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndDelete(200, "user1", "name", "Bob");
        
        assertFalse(result, "compareAndDelete should return false");
        assertEquals("Alice", db.get(300, "user1", "name"), "Value should remain");
    }
    
    @Test
    @DisplayName("Should fail compare-and-delete for non-existent key")
    void testCompareAndDeleteNonExistentKey() {
        boolean result = db.compareAndDelete(100, "nonexistent", "name", "Alice");
        
        assertFalse(result, "compareAndDelete should fail for non-existent key");
    }
    
    @Test
    @DisplayName("Should fail compare-and-delete for non-existent field")
    void testCompareAndDeleteNonExistentField() {
        db.set(100, "user1", "name", "Alice");
        boolean result = db.compareAndDelete(200, "user1", "age", "30");
        
        assertFalse(result, "compareAndDelete should fail for non-existent field");
    }
    
    @Test
    @DisplayName("Should not affect other fields after delete")
    void testCompareAndDeleteOtherFields() {
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        
        db.compareAndDelete(300, "user1", "name", "Alice");
        
        assertNull(db.get(400, "user1", "name"), "Deleted field should be null");
        assertEquals("30", db.get(400, "user1", "age"), "Other fields should remain");
    }
    
    // ========== Complex Scenario Tests ==========
    
    @Test
    @DisplayName("Should handle complex operation sequence")
    void testComplexOperations() {
        // Set initial values
        db.set(100, "user1", "name", "Alice");
        db.set(200, "user1", "age", "30");
        db.set(300, "user2", "name", "Bob");
        
        // Update with compareAndSet
        assertTrue(db.compareAndSet(400, "user1", "age", "30", "31"));
        
        // Try failed compareAndSet
        assertFalse(db.compareAndSet(500, "user1", "age", "30", "32"));
        
        // Delete a field
        assertTrue(db.compareAndDelete(600, "user1", "name", "Alice"));
        
        // Verify final state
        assertNull(db.get(700, "user1", "name"));
        assertEquals("31", db.get(700, "user1", "age"));
        assertEquals("Bob", db.get(700, "user2", "name"));
    }
    
    @Test
    @DisplayName("Should handle sequential updates")
    void testSequentialUpdates() {
        db.set(100, "counter", "value", "0");
        db.set(200, "counter", "value", "1");
        db.set(300, "counter", "value", "2");
        db.set(400, "counter", "value", "3");
        
        assertEquals("3", db.get(500, "counter", "value"), "Should have latest value");
    }
}
