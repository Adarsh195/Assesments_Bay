# Banking System Simulation - Quick Reference

## Running Tests

```bash
# Navigate to project
cd "/Users/adarshsinghai/Documents/Interview-Prep/Ebay/Ebay Simulations/Banking Simulation 01"

# Level 1: Basic Operations (createAccount, deposit, transfer)
mvn test -Dtest=Level1Test

# Level 2: Ranking (topSpenders)
mvn test -Dtest=Level2Test

# Level 3: Scheduled Payments (schedulePayment, getPaymentStatus, processScheduledPayments)
mvn test -Dtest=Level3Test

# Level 4: Account Merging (mergeAccounts)
mvn test -Dtest=Level4Test

# Run all tests
mvn clean test

# Compile only
mvn clean compile
```

## File Locations

- **Implementation:** `src/main/java/com/banking/BankingSystemImpl.java` ← **IMPLEMENT HERE**
- **Interface:** `src/main/java/com/banking/BankingSystem.java`
- **Tests:** `src/test/java/com/banking/Level*Test.java`
- **Documentation:** `README.md`

## Quick Method Reference

### Level 1: Basic Operations

```java
// Create account
boolean createAccount(String accountId, int timestamp)
// Returns: true if created, false if already exists

// Deposit money
Optional<Integer> deposit(String accountId, int timestamp, int amount)
// Returns: Optional with new balance OR empty if error

// Transfer money
Optional<Integer> transfer(String fromId, String toId, int timestamp, int amount)
// Returns: Optional with source's new balance OR empty if error
```

### Level 2: Ranking

```java
// Get top spenders
List<String> topSpenders(int timestamp, int n)
// Returns: ["accountId(outgoingAmount)", ...]
// Sorted by: amount DESC, then accountId ASC
```

### Level 3: Scheduled Payments

```java
// Schedule payment
String schedulePayment(String accountId, String targetAccId, 
                       int timestamp, int amount, double cashbackPercentage)
// Returns: unique payment ID

// Check status
String getPaymentStatus(String accountId, int timestamp, String paymentId)
// Returns: "SCHEDULED" | "PROCESSED" | "FAILED" | null

// Process payments
void processScheduledPayments(int currentTimestamp)
// Processes all payments with timestamp <= currentTimestamp
// Applies cashback for successful payments
```

### Level 4: Account Merging

```java
// Merge accounts
void mergeAccounts(String accountId1, String accountId2)
// accountId1 gets combined balance
// accountId2 is closed
// All history from accountId2 transfers to accountId1
```

## Test Count

- **Level 1:** 17 tests
- **Level 2:** 12 tests
- **Level 3:** 15 tests
- **Level 4:** 12 tests
- **Total:** 56 tests

## Expected Time

- **Level 1:** 20-30 minutes
- **Level 2:** 15-25 minutes
- **Level 3:** 25-35 minutes
- **Level 4:** 20-30 minutes
- **Total:** 80-120 minutes

## Common Edge Cases

### Level 1
- Duplicate accounts
- Non-existent accounts
- Zero/negative amounts
- Insufficient funds

### Level 2
- Empty results
- Tie-breaking by account ID
- Timestamp filtering
- Only count outgoing transactions

### Level 3
- Insufficient funds when processing
- Multiple payments at same timestamp
- Payment not yet due
- Cashback calculation

### Level 4
- Combined balances
- Transaction history transfer
- Scheduled payments from merged accounts
- Merged account becomes invalid

## Success Criteria

✅ All 56 tests pass
✅ Handles all edge cases
✅ Clean, readable code
✅ Reasonable performance

## Getting Help

1. Read the method JavaDoc in `BankingSystem.java`
2. Check examples in `README.md`
3. Look at failing test assertions
4. Review edge case handling
