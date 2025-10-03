package zm.hashcode.openpayments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

/**
 * Base class for integration tests.
 *
 * <p>
 * Integration tests verify that multiple components work together correctly. They may use real HTTP clients, mock
 * servers, or test against live Open Payments API endpoints (with appropriate credentials).
 *
 * <p>
 * Integration tests are tagged with "integration" and can be run separately from unit tests.
 */
@Tag("integration")
public abstract class BaseIntegrationTest {

    @BeforeEach
    public void setUp() {
        // Common setup for integration tests
    }

    /**
     * Helper method to check if integration tests should run against live endpoints.
     *
     * @return true if TEST_AGAINST_LIVE_API environment variable is set
     */
    protected boolean shouldTestAgainstLiveApi() {
        return Boolean.parseBoolean(System.getenv("TEST_AGAINST_LIVE_API"));
    }

    /**
     * Get test wallet address from environment or use default.
     */
    protected String getTestWalletAddress() {
        return System.getenv().getOrDefault("TEST_WALLET_ADDRESS", "https://wallet.example.com/test");
    }
}
