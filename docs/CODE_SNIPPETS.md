# Open Payments Java SDK - Usage Examples

This document provides comprehensive examples for using the Open Payments Java SDK.

## Table of Contents

- [Quick Start](#quick-start)
- [Client Setup](#client-setup)
- [Wallet Addresses](#wallet-addresses)
- [Incoming Payments](#incoming-payments)
- [Quotes](#quotes)
- [Outgoing Payments](#outgoing-payments)
- [Complete Payment Flow](#complete-payment-flow)
- [Error Handling](#error-handling)

## Quick Start

```java
import zm.hashcode.openpayments.client.OpenPaymentsClient;
import zm.hashcode.openpayments.model.Amount;

// Create the client
OpenPaymentsClient client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .privateKey(privateKey)  // Your Ed25519 private key
    .keyId("key-123")
    .build();

// Create an incoming payment
IncomingPayment payment = client.incomingPayments()
    .create(builder -> builder
        .walletAddress("https://wallet.example.com/alice")
        .incomingAmount(Amount.of("100.00", "USD", 2)))
    .join();

System.out.println("Payment created: " + payment.getId());
```

## Client Setup

### Basic Configuration

```java
import java.security.PrivateKey;
import zm.hashcode.openpayments.client.OpenPaymentsClient;
import zm.hashcode.openpayments.auth.keys.ClientKeyGenerator;

// Generate a new key pair
ClientKey clientKey = ClientKeyGenerator.generate("my-key-id");
PrivateKey privateKey = clientKey.getPrivateKey();

// Build the client
OpenPaymentsClient client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .privateKey(privateKey)
    .keyId("my-key-id")
    .build();
```

### Advanced Configuration

```java
import java.time.Duration;
import zm.hashcode.openpayments.client.OpenPaymentsClient;

OpenPaymentsClient client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .privateKey(privateKey)
    .keyId("my-key-id")
    .requestTimeout(Duration.ofSeconds(30))
    .connectionTimeout(Duration.ofSeconds(10))
    .autoRefreshTokens(true)
    .userAgent("MyApp/1.0")
    .build();
```

## Wallet Addresses

### Get Wallet Address Information

```java
import zm.hashcode.openpayments.wallet.WalletAddress;

// Retrieve wallet address metadata
WalletAddress walletAddress = client.walletAddresses()
    .get("https://wallet.example.com/alice")
    .join();

System.out.println("Asset Code: " + walletAddress.getAssetCode());
System.out.println("Asset Scale: " + walletAddress.getAssetScale());
System.out.println("Auth Server: " + walletAddress.getAuthServer());
```

### Get Public Keys

```java
import zm.hashcode.openpayments.wallet.PublicKeySet;

// Fetch public keys for signature verification
PublicKeySet keySet = client.walletAddresses()
    .getKeys("https://wallet.example.com/alice")
    .join();

System.out.println("Number of keys: " + keySet.getKeys().size());
```

## Incoming Payments

### Create an Incoming Payment

```java
import java.time.Duration;
import java.time.Instant;
import zm.hashcode.openpayments.payment.incoming.IncomingPayment;
import zm.hashcode.openpayments.model.Amount;

IncomingPayment payment = client.incomingPayments()
    .create(builder -> builder
        .walletAddress("https://wallet.example.com/alice")
        .incomingAmount(Amount.of("100.00", "USD", 2))
        .expiresAt(Instant.now().plus(Duration.ofHours(24)))
        .metadata("Invoice #12345"))
    .join();

System.out.println("Payment ID: " + payment.getId());
System.out.println("Payment URL: " + payment.getId());
```

### Retrieve an Incoming Payment

```java
// Get by string URL
IncomingPayment payment = client.incomingPayments()
    .get("https://wallet.example.com/alice/incoming-payments/123")
    .join();

// Or get by URI
URI paymentUri = URI.create("https://wallet.example.com/alice/incoming-payments/123");
IncomingPayment payment = client.incomingPayments()
    .get(paymentUri)
    .join();

System.out.println("Received Amount: " + payment.getReceivedAmount());
System.out.println("Completed: " + payment.isCompleted());
```

### List Incoming Payments

```java
import zm.hashcode.openpayments.model.PaginatedResult;

// List with default pagination (20 items)
PaginatedResult<IncomingPayment> result = client.incomingPayments()
    .list("https://wallet.example.com/alice")
    .join();

for (IncomingPayment payment : result.items()) {
    System.out.println("Payment: " + payment.getId());
}

// List with custom pagination
PaginatedResult<IncomingPayment> result = client.incomingPayments()
    .list("https://wallet.example.com/alice", null, 50)
    .join();

// Navigate to next page if available
if (result.hasMore()) {
    PaginatedResult<IncomingPayment> nextPage = client.incomingPayments()
        .list("https://wallet.example.com/alice", result.cursor(), 50)
        .join();
}
```

### Complete an Incoming Payment

```java
IncomingPayment completed = client.incomingPayments()
    .complete("https://wallet.example.com/alice/incoming-payments/123")
    .join();

System.out.println("Payment completed: " + completed.isCompleted());
```

## Quotes

### Create a Quote with Send Amount

```java
import zm.hashcode.openpayments.payment.quote.Quote;
import zm.hashcode.openpayments.model.Amount;

// Specify how much you want to send
Quote quote = client.quotes()
    .create(builder -> builder
        .walletAddress("https://wallet.example.com/alice")
        .receiver("https://wallet.example.com/bob")
        .sendAmount(Amount.of("100.00", "USD", 2)))
    .join();

System.out.println("Send: " + quote.getSendAmount());
System.out.println("Receive: " + quote.getReceiveAmount());
System.out.println("Expires: " + quote.getExpiresAt());
```

### Create a Quote with Receive Amount

```java
// Specify how much you want the receiver to get
Quote quote = client.quotes()
    .create(builder -> builder
        .walletAddress("https://wallet.example.com/alice")
        .receiver("https://wallet.example.com/bob")
        .receiveAmount(Amount.of("95.00", "EUR", 2)))
    .join();

System.out.println("Send: " + quote.getSendAmount());
System.out.println("Receive: " + quote.getReceiveAmount());
```

### Check Quote Expiration

```java
Quote quote = client.quotes()
    .get("https://wallet.example.com/alice/quotes/456")
    .join();

if (quote.isExpired()) {
    System.out.println("Quote has expired, create a new one");
} else {
    System.out.println("Quote is still valid");
}
```

## Outgoing Payments

### Create an Outgoing Payment

```java
import zm.hashcode.openpayments.payment.outgoing.OutgoingPayment;

// First, create a quote
Quote quote = client.quotes()
    .create(builder -> builder
        .walletAddress("https://wallet.example.com/alice")
        .receiver("https://wallet.example.com/bob")
        .sendAmount(Amount.of("100.00", "USD", 2)))
    .join();

// Then create the outgoing payment
OutgoingPayment payment = client.outgoingPayments()
    .create(builder -> builder
        .walletAddress("https://wallet.example.com/alice")
        .quoteId(quote.getId())
        .metadata("Payment for services"))
    .join();

System.out.println("Payment ID: " + payment.getId());
System.out.println("Receiver: " + payment.getReceiver());
System.out.println("Failed: " + payment.isFailed());
```

### Retrieve an Outgoing Payment

```java
OutgoingPayment payment = client.outgoingPayments()
    .get("https://wallet.example.com/alice/outgoing-payments/789")
    .join();

System.out.println("Send Amount: " + payment.getSendAmount());
System.out.println("Sent Amount: " + payment.getSentAmount());
System.out.println("Failed: " + payment.isFailed());
```

### List Outgoing Payments

```java
import zm.hashcode.openpayments.model.PaginatedResult;

PaginatedResult<OutgoingPayment> result = client.outgoingPayments()
    .list("https://wallet.example.com/alice")
    .join();

for (OutgoingPayment payment : result.items()) {
    System.out.println("Payment: " + payment.getId());
    System.out.println("Status: " + (payment.isFailed() ? "Failed" : "Processing"));
}
```

## Complete Payment Flow

Here's a complete example showing how to send a payment from Alice to Bob:

```java
import zm.hashcode.openpayments.client.OpenPaymentsClient;
import zm.hashcode.openpayments.model.Amount;
import zm.hashcode.openpayments.payment.quote.Quote;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPayment;

public class SendPaymentExample {
    public static void main(String[] args) {
        // Setup client
        OpenPaymentsClient client = OpenPaymentsClient.builder()
            .walletAddress("https://wallet.example.com/alice")
            .privateKey(privateKey)
            .keyId("alice-key-1")
            .build();

        try {
            // Step 1: Create a quote to get exchange rates and fees
            System.out.println("Creating quote...");
            Quote quote = client.quotes()
                .create(builder -> builder
                    .walletAddress("https://wallet.example.com/alice")
                    .receiver("https://wallet.example.com/bob")
                    .sendAmount(Amount.of("100.00", "USD", 2)))
                .join();

            System.out.println("Quote created:");
            System.out.println("  Send: " + quote.getSendAmount().value() + " " +
                             quote.getSendAmount().assetCode());
            System.out.println("  Receive: " + quote.getReceiveAmount().value() + " " +
                             quote.getReceiveAmount().assetCode());

            // Step 2: Check if quote is still valid
            if (quote.isExpired()) {
                System.err.println("Quote expired, please create a new one");
                return;
            }

            // Step 3: Create the outgoing payment
            System.out.println("Creating outgoing payment...");
            OutgoingPayment payment = client.outgoingPayments()
                .create(builder -> builder
                    .walletAddress("https://wallet.example.com/alice")
                    .quoteId(quote.getId())
                    .metadata("Payment to Bob"))
                .join();

            System.out.println("Payment created:");
            System.out.println("  ID: " + payment.getId());
            System.out.println("  Receiver: " + payment.getReceiver());
            System.out.println("  Status: " + (payment.isFailed() ? "Failed" : "Processing"));

            // Step 4: Monitor payment status
            OutgoingPayment status = client.outgoingPayments()
                .get(payment.getId())
                .join();

            System.out.println("Payment Status:");
            System.out.println("  Sent Amount: " + status.getSentAmount());
            System.out.println("  Failed: " + status.isFailed());

        } catch (Exception e) {
            System.err.println("Error sending payment: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## Error Handling

### Handling Async Exceptions

```java
import java.util.concurrent.CompletionException;

client.incomingPayments()
    .get("https://wallet.example.com/alice/incoming-payments/123")
    .exceptionally(throwable -> {
        if (throwable instanceof CompletionException) {
            Throwable cause = throwable.getCause();
            if (cause instanceof IncomingPaymentException) {
                System.err.println("Payment error: " + cause.getMessage());
            }
        }
        return null;
    })
    .join();
```

### Handling Service-Specific Exceptions

```java
import zm.hashcode.openpayments.payment.incoming.IncomingPaymentException;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentException;
import zm.hashcode.openpayments.payment.quote.QuoteException;
import zm.hashcode.openpayments.wallet.WalletAddressException;

try {
    IncomingPayment payment = client.incomingPayments()
        .create(builder -> builder
            .walletAddress("https://wallet.example.com/alice")
            .incomingAmount(Amount.of("100.00", "USD", 2)))
        .join();
} catch (CompletionException e) {
    Throwable cause = e.getCause();
    if (cause instanceof IncomingPaymentException) {
        System.err.println("Failed to create payment: " + cause.getMessage());
    } else if (cause instanceof WalletAddressException) {
        System.err.println("Invalid wallet address: " + cause.getMessage());
    }
}
```

### Timeout Handling

```java
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

try {
    IncomingPayment payment = client.incomingPayments()
        .get("https://wallet.example.com/alice/incoming-payments/123")
        .orTimeout(5, TimeUnit.SECONDS)
        .join();
} catch (CompletionException e) {
    if (e.getCause() instanceof TimeoutException) {
        System.err.println("Request timed out");
    }
}
```

## Best Practices

1. **Reuse Client Instances**: Create one client and reuse it across your application
2. **Handle Async Operations**: Use `CompletableFuture` methods like `thenApply()`, `thenCompose()` for chaining
3. **Check Quote Expiration**: Always verify quotes haven't expired before using them
4. **Monitor Payment Status**: Poll outgoing payment status to track completion
5. **Graceful Error Handling**: Always handle exceptions and provide meaningful error messages
6. **Use Pagination**: For listing operations, handle pagination properly to avoid memory issues

## Additional Resources

- [API Documentation](https://docs.openpayments.dev)
- [Open Payments Specification](https://openpayments.dev)
- [GitHub Repository](https://github.com/your-org/open-payments-java)
