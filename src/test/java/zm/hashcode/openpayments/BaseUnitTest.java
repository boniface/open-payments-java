package zm.hashcode.openpayments;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for unit tests.
 *
 * <p>
 * Unit tests should test individual components in isolation, using mocks for dependencies.
 */
public abstract class BaseUnitTest {

    @BeforeEach
    public void setUp() {
        // Common setup for unit tests
    }

    /**
     * Helper method to create test data with consistent patterns.
     */
    protected String testWalletAddress() {
        return "https://wallet.example.com/alice";
    }

    protected String testWalletAddress(String accountName) {
        return "https://wallet.example.com/" + accountName;
    }

    protected String testPaymentUrl() {
        return "https://wallet.example.com/incoming-payments/test-payment-id";
    }
}
