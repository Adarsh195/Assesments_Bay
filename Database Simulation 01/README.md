# In-Memory Database Simulation - Interview Coding Assessment

This is a comprehensive coding assessment that tests your ability to implement an in-memory database with progressively complex features across 4 levels.

## Overview

You'll implement an in-memory database with the following capabilities:
- **Level 1**: Basic CRUD operations (set, get, compareAndSet, compareAndDelete)
- **Level 2**: Scanning and filtering (scan, scanByPrefix)
- **Level 3**: TTL support (time-to-live with automatic expiration)
- **Level 4**: Time-travel queries (retrieve historical values)

## Project Structure

```
Database Simulation 01/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── database/
│   │               ├── InMemoryDatabase.java       # Interface with all methods
│   │               └── InMemoryDatabaseImpl.java   # YOUR IMPLEMENTATION HERE
│   └── test/
│       └── java/
│           └── com/
│               └── database/
│                   ├── Level1Test.java             # Basic operations tests
│                   ├── Level2Test.java             # Scanning tests
│                   ├── Level3Test.java             # TTL tests
│                   └── Level4Test.java             # Time-travel tests
└── README.md                        # This file
```

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Setup
1. Navigate to the project directory:
   ```bash
   cd "Database Simulation 01"
   ```

2. Verify the project compiles:
   ```bash
   mvn clean compile
   ```

### Running Tests

Run tests level by level to validate your implementation:

```bash
# Level 1: Basic Operations
mvn test -Dtest=Level1Test

# Level 2: Scanning
mvn test -Dtest=Level2Test

# Level 3: TTL Support
mvn test -Dtest=Level3Test

# Level 4: Time-Travel
mvn test -Dtest=Level4Test

# Run all tests
mvn clean test
```

## Data Structure

The database stores data in a nested structure:

```
Key -> Field -> Value

Example:
"user1" -> "name" -> "Alice"
"user1" -> "age" -> "30"
"user2" -> "name" -> "Bob"
```

Think of it as a two-level map where each key contains multiple fields, and each field has a value.

## Implementation Requirements

### Level 1: Basic Operations

Implement core CRUD operations:

#### `void set(int timestamp, String key, String field, String value)`
- Sets a field in a record to the specified value
- Creates the key and/or field if they don't exist

**Example:**
```java
db.set(100, "user1", "name", "Alice");
db.set(200, "user1", "age", "30");
```

#### `String get(int timestamp, String key, String field)`
- Returns the value of a field
- Returns `null` if the key or field doesn't exist
- Returns `null` if the field has expired (Level 3)

**Example:**
```java
db.set(100, "user1", "name", "Alice");
String value = db.get(200, "user1", "name");  // Returns "Alice"
String missing = db.get(200, "user1", "age"); // Returns null
```

#### `boolean compareAndSet(int timestamp, String key, String field, String expectedValue, String newValue)`
- Atomically compares and sets a value
- Only updates if the current value equals `expectedValue`
- Returns `true` if successful, `false` otherwise
- If `expectedValue` is `null`, succeed only if field doesn't exist (allows creation)

**Example:**
```java
db.set(100, "counter", "value", "5");
db.compareAndSet(200, "counter", "value", "5", "6");  // Returns true, updates to "6"
db.compareAndSet(300, "counter", "value", "5", "7");  // Returns false, value is "6" not "5"

// Create new field with null expected value
db.compareAndSet(400, "user1", "city", null, "NYC");  // Returns true, creates field
```

#### `boolean compareAndDelete(int timestamp, String key, String field, String expectedValue)`
- Atomically compares and deletes a field
- Only deletes if the current value equals `expectedValue`
- Returns `true` if successful, `false` otherwise

**Example:**
```java
db.set(100, "user1", "temp", "delete_me");
db.compareAndDelete(200, "user1", "temp", "delete_me");  // Returns true, deletes field
db.get(300, "user1", "temp");  // Returns null (field deleted)
```

---

### Level 2: Scanning Operations

Implement operations to retrieve multiple fields:

#### `List<String> scan(int timestamp, String key)`
- Returns all fields in a record
- Format: `["field1(value1)", "field2(value2)", ...]`
- **Must be sorted by field name** in ascending order
- Returns empty list if key doesn't exist
- Excludes expired fields (Level 3)

**Example:**
```java
db.set(100, "user1", "name", "Alice");
db.set(200, "user1", "age", "30");
db.set(300, "user1", "city", "NYC");

List<String> fields = db.scan(400, "user1");
// Returns: ["age(30)", "city(NYC)", "name(Alice)"]
// Note: Sorted alphabetically by field name
```

#### `List<String> scanByPrefix(int timestamp, String key, String prefix)`
- Returns fields that start with the given prefix
- Format: `["field1(value1)", "field2(value2)", ...]`
- **Must be sorted by field name** in ascending order
- Returns empty list if no matches or key doesn't exist
- Empty prefix matches all fields (same as `scan`)

**Example:**
```java
db.set(100, "config", "app.server.host", "localhost");
db.set(200, "config", "app.server.port", "8080");
db.set(300, "config", "app.db.host", "dbhost");
db.set(400, "config", "logging.level", "INFO");

List<String> serverConfig = db.scanByPrefix(500, "config", "app.server.");
// Returns: ["app.server.host(localhost)", "app.server.port(8080)"]

List<String> appConfig = db.scanByPrefix(500, "config", "app.");
// Returns: ["app.db.host(dbhost)", "app.server.host(localhost)", "app.server.port(8080)"]
```

---

### Level 3: TTL (Time-To-Live) Operations

Implement automatic expiration of fields:

#### `void setWithTTL(int timestamp, String key, String field, String value, int ttl)`
- Sets a field with a time-to-live
- Field expires at `timestamp + ttl`
- After expiration, field is treated as non-existent
- Expired fields are not returned by `get` or `scan`

**Example:**
```java
db.setWithTTL(100, "session", "token", "abc123", 50);
// Token expires at timestamp 150 (100 + 50)

db.get(140, "session", "token");  // Returns "abc123" (before expiration)
db.get(150, "session", "token");  // Returns null (at expiration)
db.get(200, "session", "token");  // Returns null (after expiration)
```

#### `boolean compareAndSetWithTTL(int timestamp, String key, String field, String expectedValue, String newValue, int ttl)`
- Atomically compares and sets with TTL
- Combines `compareAndSet` with `setWithTTL`
- If successful, field expires at `timestamp + ttl`

**Example:**
```java
db.set(100, "cache", "key", "old_value");
db.compareAndSetWithTTL(200, "cache", "key", "old_value", "new_value", 100);
// Returns true, "new_value" expires at timestamp 300 (200 + 100)

db.get(250, "cache", "key");  // Returns "new_value"
db.get(300, "cache", "key");  // Returns null (expired)
```

**Important Notes:**
- TTL = 0 means immediate expiration
- Regular `set()` removes any existing TTL
- `setWithTTL()` replaces both value and TTL
- Expired fields should not appear in scan results
- Operations on expired fields behave as if the field doesn't exist

---

### Level 4: Time-Travel Operations

Implement historical data tracking and queries:

#### `void setAt(int currentTimestamp, String key, String field, String value, int setTimestamp)`
- Records a value as if it was set at `setTimestamp`
- Allows inserting historical data
- The operation happens at `currentTimestamp` but records history at `setTimestamp`

**Example:**
```java
// At time 500, record that value was "Alice" at time 100
db.setAt(500, "user", "name", "Alice", 100);

// Can now query what the value was at time 100
db.getAt(600, "user", "name", 100);  // Returns "Alice"
```

#### `String getAt(int currentTimestamp, String key, String field, int queryTimestamp)`
- Retrieves the value as it was at `queryTimestamp`
- Returns the most recent value at or before `queryTimestamp`
- Returns `null` if no value existed at that time
- Enables "time-travel" queries to see historical data

**Example:**
```java
db.set(100, "price", "stock", "100");
db.set(200, "price", "stock", "105");
db.set(300, "price", "stock", "102");

// Query historical prices
db.getAt(400, "price", "stock", 150);  // Returns "100" (value at time 100)
db.getAt(400, "price", "stock", 250);  // Returns "105" (value at time 200)
db.getAt(400, "price", "stock", 350);  // Returns "102" (value at time 300)
db.getAt(400, "price", "stock", 50);   // Returns null (before first set)
```

**Complex Example:**
```java
db.set(100, "user", "status", "active");
db.set(200, "user", "status", "inactive");
db.compareAndDelete(300, "user", "status", "inactive");
db.set(400, "user", "status", "active");

// Time-travel queries
db.getAt(500, "user", "status", 150);  // Returns "active"
db.getAt(500, "user", "status", 250);  // Returns "inactive"
db.getAt(500, "user", "status", 350);  // Returns null (deleted)
db.getAt(500, "user", "status", 450);  // Returns "active" (recreated)
```

**TTL and Time-Travel:**
```java
db.setWithTTL(100, "temp", "key", "value", 50);  // Expires at 150

db.getAt(200, "temp", "key", 120);  // Returns "value" (before expiration)
db.getAt(200, "temp", "key", 150);  // Returns null (respecting TTL in history)
```

---

## Implementation Tips

### Data Structure Suggestions

```java
// Level 1: Basic storage
Map<String, Map<String, String>> data;  // key -> field -> value

// Level 3: TTL support
Map<String, Map<String, Integer>> ttlData;  // key -> field -> expirationTime

// Level 4: History tracking
Map<String, Map<String, List<HistoryEntry>>> history;  // key -> field -> [entries]

class HistoryEntry {
    int timestamp;
    String value;
    boolean isDeleted;
    Integer expiresAt;  // null if no TTL
}
```

### Key Considerations

1. **Nested Structure**: Key contains fields, fields contain values
2. **TTL Expiration**: Check expiration in all read operations
3. **History Tracking**: Maintain all value changes with timestamps
4. **Sorting**: Scan operations must return sorted results
5. **Null Handling**: `null` has special meaning in compareAndSet
6. **Atomic Operations**: Compare-and-set/delete are atomic
7. **Time-Travel**: Needs complete history of all changes

### Common Pitfalls

❌ **Don't forget to check TTL** in get, scan, and scanByPrefix
❌ **Don't forget to sort** scan results by field name
❌ **Don't forget to track history** for time-travel queries
❌ **Don't forget TTL in historical queries** (expired fields should return null)

---

## Testing Strategy

### Recommended Approach

1. **Start with Level 1**: Get basic operations working
2. **Test incrementally**: Run tests after each method
3. **Use failing tests as guides**: Read test names and assertions
4. **Level by level**: Complete one level before moving to the next
5. **Edge cases**: Ensure all edge case tests pass

### Example Test Execution

```bash
# Start with Level 1
mvn test -Dtest=Level1Test

# Expected initial output (all tests fail):
# Tests run: 23, Failures: 0, Errors: 23, Skipped: 0

# After implementing set and get:
# Tests run: 23, Failures: 0, Errors: 15, Skipped: 0

# Continue until all pass:
# Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
```

---

## Assessment Criteria

Your implementation will be evaluated on:

✅ **Correctness**: All test cases pass
✅ **Edge Case Handling**: Properly handles invalid inputs and edge cases
✅ **Code Quality**: Clean, readable, well-organized code
✅ **Efficiency**: Reasonable time/space complexity
✅ **Completeness**: All 4 levels implemented

---

## Test Count Summary

- **Level 1**: 23 tests (Basic operations)
- **Level 2**: 18 tests (Scanning)
- **Level 3**: 20 tests (TTL)
- **Level 4**: 16 tests (Time-travel)
- **Total**: 77 tests

---

## Time Expectations

- **Level 1**: 25-35 minutes
- **Level 2**: 15-20 minutes
- **Level 3**: 25-35 minutes
- **Level 4**: 30-40 minutes
- **Total**: 95-130 minutes

---

## Need Help?

If you're stuck:
1. Re-read the method documentation in `InMemoryDatabase.java`
2. Review the examples in this README
3. Examine the failing test assertions carefully
4. Check your data structure design
5. Verify edge case handling

Good luck! 🚀
