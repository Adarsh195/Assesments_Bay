package com.database;

import java.util.List;

/**
 * In-Memory Database Interface for Interview Coding Assessment
 * 
 * This interface defines all operations required across 4 difficulty levels:
 * - Level 1: Basic CRUD operations (set, get, compareAndSet, compareAndDelete)
 * - Level 2: Scanning and filtering (scan, scanByPrefix)
 * - Level 3: TTL support (setWithTTL, compareAndSetWithTTL)
 * - Level 4: Time-travel queries (setAt, getAt)
 * 
 * Data Structure:
 * The database stores records with the following structure:
 * Key -> Field -> Value
 * 
 * Example:
 * "user1" -> "name" -> "Alice"
 * "user1" -> "age" -> "30"
 * "user2" -> "name" -> "Bob"
 */
public interface InMemoryDatabase {
    
    // ========== LEVEL 1: Basic Operations ==========
    
    /**
     * Sets a field in a record to the specified value.
     * If the key or field doesn't exist, it should be created.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param field The field name within the record
     * @param value The value to set
     */
    void set(int timestamp, String key, String field, String value);
    
    /**
     * Gets the value of a field in a record.
     * Returns null if the key or field doesn't exist.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param field The field name within the record
     * @return The value of the field, or null if not found
     */
    String get(int timestamp, String key, String field);
    
    /**
     * Atomically compares and sets a field value.
     * Only sets the new value if the current value equals the expected value.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param field The field name within the record
     * @param expectedValue The expected current value
     * @param newValue The new value to set if comparison succeeds
     * @return true if the value was updated, false otherwise
     */
    boolean compareAndSet(int timestamp, String key, String field, String expectedValue, String newValue);
    
    /**
     * Atomically compares and deletes a field.
     * Only deletes the field if the current value equals the expected value.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param field The field name within the record
     * @param expectedValue The expected current value
     * @return true if the field was deleted, false otherwise
     */
    boolean compareAndDelete(int timestamp, String key, String field, String expectedValue);
    
    // ========== LEVEL 2: Scanning Operations ==========
    
    /**
     * Scans all fields in a record and returns them as a list.
     * Each element in the list is formatted as "field(value)".
     * The list should be sorted by field name in ascending order.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @return List of fields in format "field(value)", sorted by field name.
     *         Returns empty list if key doesn't exist.
     */
    List<String> scan(int timestamp, String key);
    
    /**
     * Scans all fields in a record that start with the given prefix.
     * Each element in the list is formatted as "field(value)".
     * The list should be sorted by field name in ascending order.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param prefix The prefix to filter field names
     * @return List of matching fields in format "field(value)", sorted by field name.
     *         Returns empty list if key doesn't exist or no fields match.
     */
    List<String> scanByPrefix(int timestamp, String key, String prefix);
    
    // ========== LEVEL 3: TTL Operations ==========
    
    /**
     * Sets a field in a record to the specified value with a time-to-live (TTL).
     * The field will automatically expire after TTL seconds from the current timestamp.
     * After expiration, the field should be treated as if it doesn't exist.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param field The field name within the record
     * @param value The value to set
     * @param ttl Time-to-live in seconds (the field expires at timestamp + ttl)
     */
    void setWithTTL(int timestamp, String key, String field, String value, int ttl);
    
    /**
     * Atomically compares and sets a field value with a TTL.
     * Only sets the new value if the current value equals the expected value.
     * If successful, the field will expire after TTL seconds.
     * 
     * @param timestamp The timestamp of the operation
     * @param key The record key
     * @param field The field name within the record
     * @param expectedValue The expected current value
     * @param newValue The new value to set if comparison succeeds
     * @param ttl Time-to-live in seconds
     * @return true if the value was updated, false otherwise
     */
    boolean compareAndSetWithTTL(int timestamp, String key, String field, 
                                 String expectedValue, String newValue, int ttl);
    
    // ========== LEVEL 4: Time-Travel Operations ==========
    
    /**
     * Sets a field value explicitly at a past timestamp.
     * This allows inserting historical data into the database.
     * Note: This operation happens at 'currentTimestamp' but stores the value as if
     * it was set at 'setTimestamp'.
     * 
     * @param currentTimestamp The current timestamp when this operation is called
     * @param key The record key
     * @param field The field name within the record
     * @param value The value to set
     * @param setTimestamp The timestamp at which the value should be recorded
     */
    void setAt(int currentTimestamp, String key, String field, String value, int setTimestamp);
    
    /**
     * Gets the value of a field as it was at a specific past timestamp.
     * This enables time-travel queries to retrieve historical data.
     * 
     * @param currentTimestamp The current timestamp when this operation is called
     * @param key The record key
     * @param field The field name within the record
     * @param queryTimestamp The timestamp for which to retrieve the value
     * @return The value of the field at the specified timestamp, or null if not found
     */
    String getAt(int currentTimestamp, String key, String field, int queryTimestamp);
}
