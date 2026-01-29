# In-Memory Database Simulation - Quick Reference

## Running Tests

```bash
# Navigate to project
cd "/Users/adarshsinghai/Documents/Interview-Prep/Ebay/Ebay Simulations/Database Simulation 01"

# Level 1: Basic Operations (set, get, compareAndSet, compareAndDelete)
mvn test -Dtest=Level1Test

# Level 2: Scanning (scan, scanByPrefix)
mvn test -Dtest=Level2Test

# Level 3: TTL (setWithTTL, compareAndSetWithTTL)
mvn test -Dtest=Level3Test

# Level 4: Time-Travel (setAt, getAt)
mvn test -Dtest=Level4Test

# Run all tests
mvn clean test

# Compile only
mvn clean compile
```

## File Locations

- **Implementation:** `src/main/java/com/database/InMemoryDatabaseImpl.java` ← **IMPLEMENT HERE**
- **Interface:** `src/main/java/com/database/InMemoryDatabase.java`
- **Tests:** `src/test/java/com/database/Level*Test.java`
- **Documentation:** `README.md`

## Data Structure

```
Key -> Field -> Value

Example:
"user1" -> "name" -> "Alice"
"user1" -> "age" -> "30"
"user2" -> "name" -> "Bob"
```

## Quick Method Reference

### Level 1: Basic Operations

```java
// Set a field value
void set(int timestamp, String key, String field, String value)

// Get a field value
String get(int timestamp, String key, String field)
// Returns: value or null

// Compare and set (atomic)
boolean compareAndSet(int timestamp, String key, String field, 
                     String expectedValue, String newValue)
// Returns: true if updated, false otherwise
// Note: expectedValue = null means "field doesn't exist"

// Compare and delete (atomic)
boolean compareAndDelete(int timestamp, String key, String field, 
                        String expectedValue)
// Returns: true if deleted, false otherwise
```

### Level 2: Scanning

```java
// Scan all fields
List<String> scan(int timestamp, String key)
// Returns: ["field1(value1)", "field2(value2)", ...]
// MUST BE SORTED by field name (ascending)

// Scan fields with prefix
List<String> scanByPrefix(int timestamp, String key, String prefix)
// Returns: fields starting with prefix, SORTED
// Empty prefix = all fields
```

### Level 3: TTL (Time-To-Live)

```java
// Set with TTL
void setWithTTL(int timestamp, String key, String field, String value, int ttl)
// Field expires at: timestamp + ttl

// Compare and set with TTL
boolean compareAndSetWithTTL(int timestamp, String key, String field,
                            String expectedValue, String newValue, int ttl)
// Combines compareAndSet + setWithTTL

// Important: Expired fields return null and don't appear in scans
```

### Level 4: Time-Travel

```java
// Set at specific historical timestamp
void setAt(int currentTimestamp, String key, String field, 
          String value, int setTimestamp)
// Record value as if it was set at setTimestamp

// Get value at specific timestamp (time-travel query)
String getAt(int currentTimestamp, String key, String field, 
            int queryTimestamp)
// Returns: value as it was at queryTimestamp
// Returns: latest value at or before queryTimestamp
```

## Test Count

- **Level 1**: 23 tests
- **Level 2**: 18 tests
- **Level 3**: 20 tests
- **Level 4**: 16 tests
- **Total**: 77 tests

## Expected Time

- **Level 1**: 25-35 minutes
- **Level 2**: 15-20 minutes
- **Level 3**: 25-35 minutes
- **Level 4**: 30-40 minutes
- **Total**: 95-130 minutes

## Data Structure Suggestions

```java
// Basic storage
Map<String, Map<String, String>> data;  
// key -> field -> value

// TTL tracking
Map<String, Map<String, Integer>> ttlData;  
// key -> field -> expirationTime

// History tracking (for time-travel)
Map<String, Map<String, List<HistoryEntry>>> history;
// key -> field -> list of changes

class HistoryEntry {
    int timestamp;
    String value;
    boolean isDeleted;
    Integer expiresAt;  // null if no TTL
}
```

## Common Edge Cases

### Level 1
- Null values (special meaning in compareAndSet)
- Non-existent keys/fields
- Empty string values
- Updating existing values

### Level 2
- Empty results
- Sorting by field name (alphabetical)
- Empty prefix (matches all)
- Partial deletions (some fields remain)

### Level 3
- TTL = 0 (immediate expiration)
- Checking expiration in get/scan
- Regular set removes TTL
- setWithTTL replaces TTL
- Expired fields not in scan

### Level 4
- Query before first set
- Find latest value ≤ query timestamp
- Deletion in history
- Recreation after deletion
- TTL in historical context
- Insert into middle of history

## Success Criteria

✅ All 77 tests pass
✅ Handles all edge cases
✅ Scan results are sorted
✅ TTL expiration works correctly
✅ Time-travel queries accurate
✅ Clean, efficient code

## Implementation Tips

1. **Level 1**: Start simple with nested maps
2. **Level 2**: Use TreeMap for automatic sorting
3. **Level 3**: Check expiration in all read operations
4. **Level 4**: Maintain complete history list per field

## Getting Help

1. Read method JavaDoc in `InMemoryDatabase.java`
2. Check examples in `README.md`
3. Look at failing test assertions
4. Review data structure design
5. Verify expiration/history logic
