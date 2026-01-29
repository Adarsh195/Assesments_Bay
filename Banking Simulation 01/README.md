# Banking System Simulation - Interview Coding Assessment

This is a comprehensive coding assessment that tests your ability to implement a banking system with progressively complex features across 4 levels.

## Overview

You'll implement a banking system with the following capabilities:
- **Level 1**: Basic account operations (create, deposit, transfer)
- **Level 2**: Account ranking (top spenders)
- **Level 3**: Scheduled payments with cashback
- **Level 4**: Account merging

## Project Structure

```
Banking Simulation 01/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── banking/
│   │               ├── BankingSystem.java       # Interface with all methods
│   │               └── BankingSystemImpl.java   # YOUR IMPLEMENTATION HERE
│   └── test/
│       └── java/
│           └── com/
│               └── banking/
│                   ├── Level1Test.java          # Basic operations tests
│                   ├── Level2Test.java          # Ranking tests
│                   ├── Level3Test.java          # Scheduled payments tests
│                   └── Level4Test.java          # Account merging tests
└── README.md                        # This file
```

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Setup
1. Navigate to the project directory:
   ```bash
   cd "Banking Simulation 01"
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

# Level 2: Ranking
mvn test -Dtest=Level2Test

# Level 3: Scheduled Payments
mvn test -Dtest=Level3Test

# Level 4: Account Merging
mvn test -Dtest=Level4Test

# Run all tests
mvn clean test
```

## Implementation Requirements

### Level 1: Basic Banking Operations

Implement these three core methods:

#### `boolean createAccount(String accountId, int timestamp)`
- Creates a new account with the given ID
- Returns `true` if created successfully
- Returns `false` if account already exists

**Example:**
```java
bank.createAccount("acc1", 1000);  // returns true
bank.createAccount("acc1", 2000);  // returns false (duplicate)
```

#### `Optional<Integer> deposit(String accountId, int timestamp, int amount)`
- Deposits money into the specified account
- Returns `Optional` with new balance if successful
- Returns empty `Optional` if:
  - Account doesn't exist
  - Amount is not positive (≤ 0)

**Example:**
```java
bank.createAccount("acc1", 1000);
bank.deposit("acc1", 1100, 500);      // returns Optional[500]
bank.deposit("acc1", 1200, 300);      // returns Optional[800]
bank.deposit("nonexistent", 1300, 100); // returns Optional.empty()
bank.deposit("acc1", 1400, 0);        // returns Optional.empty()
bank.deposit("acc1", 1500, -50);      // returns Optional.empty()
```

#### `Optional<Integer> transfer(String fromId, String toId, int timestamp, int amount)`
- Transfers money from one account to another
- Returns `Optional` with new balance of source account if successful
- Returns empty `Optional` if:
  - Either account doesn't exist
  - Amount is not positive (≤ 0)
  - Insufficient funds in source account

**Example:**
```java
bank.createAccount("acc1", 1000);
bank.createAccount("acc2", 1000);
bank.deposit("acc1", 1100, 1000);

bank.transfer("acc1", "acc2", 1200, 300);  // returns Optional[700]
bank.transfer("acc1", "acc2", 1300, 800);  // returns Optional.empty() (insufficient funds)
bank.transfer("acc1", "nonexistent", 1400, 100); // returns Optional.empty()
```

---

### Level 2: Account Ranking

Implement the top spenders ranking system:

#### `List<String> topSpenders(int timestamp, int n)`
- Returns top N accounts based on **outgoing transactions only**
- Only counts transfers FROM an account (not TO an account)
- Only considers transactions up to the given timestamp
- Results sorted by:
  1. Total outgoing amount (descending)
  2. Account ID (ascending) for ties
- Format: `"accountId(totalOutgoing)"`

**Example:**
```java
bank.createAccount("alice", 1000);
bank.createAccount("bob", 1000);
bank.createAccount("charlie", 1000);

bank.deposit("alice", 1100, 5000);
bank.deposit("bob", 1100, 5000);

bank.transfer("alice", "charlie", 1200, 2000);
bank.transfer("bob", "charlie", 1300, 1500);
bank.transfer("alice", "bob", 1400, 1000);

bank.topSpenders(1500, 3);
// Returns: ["alice(3000)", "bob(1500)"]
// charlie has received money but not sent any
```

**Sorting Example (with ties):**
```java
// acc1: 500 outgoing
// acc2: 500 outgoing (same as acc1)
// acc3: 300 outgoing

bank.topSpenders(2000, 3);
// Returns: ["acc1(500)", "acc2(500)", "acc3(300)"]
// acc1 comes before acc2 due to alphabetical ordering
```

---

### Level 3: Scheduled Payments with Cashback

Implement scheduled payment functionality:

#### `String schedulePayment(String accountId, String targetAccId, int timestamp, int amount, double cashbackPercentage)`
- Schedules a payment to be processed at the specified timestamp
- Returns a unique payment ID for tracking
- Payment is not executed immediately
- Cashback percentage: 0-100 (e.g., 10.0 = 10% cashback)

#### `String getPaymentStatus(String accountId, int timestamp, String paymentId)`
- Checks the status of a scheduled payment
- Returns one of:
  - `"SCHEDULED"` - Payment hasn't been processed yet
  - `"PROCESSED"` - Payment was successfully executed
  - `"FAILED"` - Payment failed (e.g., insufficient funds)
  - `null` - Payment ID doesn't exist for this account

#### `void processScheduledPayments(int currentTimestamp)`
- Processes all scheduled payments with timestamp ≤ currentTimestamp
- For each successful payment:
  1. Transfer amount from source to destination
  2. Apply cashback to source account
- Mark failed payments as `"FAILED"` (e.g., insufficient funds)
- Only process each payment once

**Example:**
```java
bank.createAccount("acc1", 1000);
bank.createAccount("acc2", 1000);
bank.deposit("acc1", 1100, 1000);

// Schedule payment with 10% cashback
String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 10.0);

bank.getPaymentStatus("acc1", 1500, paymentId);  // returns "SCHEDULED"

// Process payments due at or before timestamp 2000
bank.processScheduledPayments(2000);

bank.getPaymentStatus("acc1", 2100, paymentId);  // returns "PROCESSED"

// acc1 balance: 1000 - 500 + 50 (10% cashback) = 550
// acc2 balance: 0 + 500 = 500
```

**Failed Payment Example:**
```java
bank.createAccount("acc1", 1000);
bank.createAccount("acc2", 1000);
bank.deposit("acc1", 1100, 100);  // Only 100 available

String paymentId = bank.schedulePayment("acc1", "acc2", 2000, 500, 5.0);
bank.processScheduledPayments(2000);

bank.getPaymentStatus("acc1", 2100, paymentId);  // returns "FAILED"
// acc1 balance unchanged: 100
```

---

### Level 4: Account Merging

Implement account merging functionality:

#### `void mergeAccounts(String accountId1, String accountId2)`
- Merges `accountId2` into `accountId1`
- After merging:
  1. `accountId1` has combined balance of both accounts
  2. `accountId2` is closed/removed
  3. All transaction history from `accountId2` is attributed to `accountId1`
  4. All scheduled payments from `accountId2` transfer to `accountId1`
  5. Future operations on `accountId2` should fail

**Example:**
```java
bank.createAccount("acc1", 1000);
bank.createAccount("acc2", 1000);
bank.createAccount("acc3", 1000);

bank.deposit("acc1", 1100, 1000);
bank.deposit("acc2", 1100, 500);

// acc2 has some transaction history
bank.transfer("acc2", "acc3", 1200, 200);  // acc2 spent 200

// Before merge
bank.topSpenders(1300, 5);  // includes "acc2(200)"

// Merge acc2 into acc1
bank.mergeAccounts("acc1", "acc2");

// After merge
// - acc1 balance: 1000 + (500 - 200) = 1300
// - acc1 spending history includes acc2's 200
// - acc2 no longer exists

bank.topSpenders(1400, 5);  // shows "acc1(200)", not "acc2(200)"
bank.deposit("acc2", 1500, 100);  // returns Optional.empty() (acc2 doesn't exist)
```

**Complex Merge Example:**
```java
bank.createAccount("acc1", 1000);
bank.createAccount("acc2", 1000);
bank.createAccount("acc3", 1000);

bank.deposit("acc1", 1100, 2000);
bank.deposit("acc2", 1100, 1000);

// Schedule payment from acc2
bank.schedulePayment("acc2", "acc3", 2000, 500, 10.0);

// Merge acc2 into acc1
bank.mergeAccounts("acc1", "acc2");

// Process scheduled payments
bank.processScheduledPayments(2000);

// The payment from acc2 is now processed as if from acc1
// acc1 balance: 2000 + 1000 - 500 + 50 (cashback) = 2550
```

---

## Implementation Tips

### Data Structure Suggestions

You may want to use:
- `Map<String, Account>` to store accounts
- `List<Transaction>` to track transaction history
- `Map<String, ScheduledPayment>` to manage scheduled payments
- Helper classes for `Account`, `Transaction`, and `ScheduledPayment`

### Key Considerations

1. **Thread Safety**: Not required for this assessment
2. **Edge Cases**: Handle all edge cases mentioned in method documentation
3. **Data Integrity**: Ensure balances are always accurate after operations
4. **Transaction History**: Maintain enough data to support `topSpenders` and merging
5. **Payment IDs**: Generate unique IDs (simple counter or UUID works)
6. **Cashback Precision**: Use `double` for cashback percentage, cast to `int` for amount

### Common Pitfalls

❌ **Don't count incoming transfers as spending** in `topSpenders`
❌ **Don't forget to apply cashback** after successful scheduled payments
❌ **Don't process scheduled payments** before their timestamp
❌ **Don't forget to update transaction history** when merging accounts

---

## Testing Strategy

### Recommended Approach

1. **Start with Level 1**: Get basic operations working first
2. **Test incrementally**: Run tests after implementing each method
3. **Use failing tests as guides**: Read test names and assertions to understand requirements
4. **Level by level**: Complete one level before moving to the next
5. **Edge cases**: Make sure all edge case tests pass

### Debugging Tips

If tests fail:
1. Read the test name - it describes what should happen
2. Look at the assertion that failed
3. Check the example scenarios in this README
4. Add print statements to trace your logic
5. Verify you're handling edge cases (null checks, empty checks, etc.)

### Example Test Execution

```bash
# Start with Level 1
mvn test -Dtest=Level1Test

# Expected initial output (all tests will fail):
# Tests run: 15, Failures: 0, Errors: 15, Skipped: 0

# After implementing createAccount:
# Tests run: 15, Failures: 0, Errors: 12, Skipped: 0

# Continue until all pass:
# Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

---

## Assessment Criteria

Your implementation will be evaluated on:

✅ **Correctness**: All test cases pass
✅ **Edge Case Handling**: Properly handles invalid inputs and edge cases
✅ **Code Quality**: Clean, readable, well-organized code
✅ **Efficiency**: Reasonable time/space complexity (no need for over-optimization)
✅ **Completeness**: All 4 levels implemented

---

## Time Expectations

- **Level 1**: 20-30 minutes
- **Level 2**: 15-25 minutes
- **Level 3**: 25-35 minutes
- **Level 4**: 20-30 minutes
- **Total**: 80-120 minutes

---

## Need Help?

If you're stuck:
1. Re-read the method documentation in `BankingSystem.java`
2. Review the examples in this README
3. Examine the failing test assertions carefully
4. Check your edge case handling

Good luck! 🚀
