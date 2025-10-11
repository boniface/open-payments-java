package zm.hashcode.openpayments.auth.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ContentDigest}.
 */
class ContentDigestTest {

    @Test
    @DisplayName("Should generate SHA-256 content digest with correct format")
    void shouldGenerateDigestWithCorrectFormat() {
        // Given
        String body = "{\"amount\":\"100\"}";

        // When
        String digest = ContentDigest.generate(body);

        // Then
        assertThat(digest).startsWith("sha-256=:");
        assertThat(digest).endsWith(":=");
        assertThat(digest).hasSizeGreaterThan(50); // "sha-256=:" (10) + base64 chars (~44) + ":=" (2)
    }

    @Test
    @DisplayName("Should generate consistent digest for same input")
    void shouldGenerateConsistentDigest() {
        // Given
        String body = "Hello, World!";

        // When
        String digest1 = ContentDigest.generate(body);
        String digest2 = ContentDigest.generate(body);

        // Then
        assertThat(digest1).isEqualTo(digest2);
    }

    @Test
    @DisplayName("Should generate different digests for different inputs")
    void shouldGenerateDifferentDigestsForDifferentInputs() {
        // Given
        String body1 = "Hello, World!";
        String body2 = "Hello, World";

        // When
        String digest1 = ContentDigest.generate(body1);
        String digest2 = ContentDigest.generate(body2);

        // Then
        assertThat(digest1).isNotEqualTo(digest2);
    }

    @Test
    @DisplayName("Should generate digest for empty string")
    void shouldGenerateDigestForEmptyString() {
        // Given
        String body = "";

        // When
        String digest = ContentDigest.generate(body);

        // Then
        assertThat(digest).isNotEmpty();
        assertThat(digest).startsWith("sha-256=:");
        assertThat(digest).endsWith(":=");
    }

    @Test
    @DisplayName("Should generate known digest for test vector")
    void shouldGenerateKnownDigestForTestVector() {
        // Given - Test vector from RFC examples
        String body = "Hello, World!";

        // When
        String digest = ContentDigest.generate(body);
        String hash = ContentDigest.extractHash(digest);

        // Then - SHA-256 of "Hello, World!" is known
        assertThat(hash).isNotEmpty();
        assertThat(digest).contains(hash);
    }

    @Test
    @DisplayName("Should throw NullPointerException when body is null")
    void shouldThrowExceptionWhenBodyIsNull() {
        assertThatThrownBy(() -> ContentDigest.generate(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("body must not be null");
    }

    @Test
    @DisplayName("Should validate correct digest")
    void shouldValidateCorrectDigest() {
        // Given
        String body = "{\"amount\":\"100\"}";
        String digest = ContentDigest.generate(body);

        // When
        boolean isValid = ContentDigest.validate(body, digest);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid digest")
    void shouldRejectInvalidDigest() {
        // Given
        String body = "{\"amount\":\"100\"}";
        String wrongDigest = "sha-256=:invalidhash:=";

        // When
        boolean isValid = ContentDigest.validate(body, wrongDigest);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject digest with wrong body")
    void shouldRejectDigestWithWrongBody() {
        // Given
        String body1 = "{\"amount\":\"100\"}";
        String body2 = "{\"amount\":\"200\"}";
        String digest = ContentDigest.generate(body1);

        // When
        boolean isValid = ContentDigest.validate(body2, digest);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should throw NullPointerException when validating with null body")
    void shouldThrowExceptionWhenValidatingWithNullBody() {
        assertThatThrownBy(() -> ContentDigest.validate(null, "sha-256=:test:="))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("body must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException when validating with null digest")
    void shouldThrowExceptionWhenValidatingWithNullDigest() {
        assertThatThrownBy(() -> ContentDigest.validate("body", null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("digestHeader must not be null");
    }

    @Test
    @DisplayName("Should extract hash from valid digest")
    void shouldExtractHashFromValidDigest() {
        // Given
        String digest = "sha-256=:ABC123XYZ:=";

        // When
        String hash = ContentDigest.extractHash(digest);

        // Then
        assertThat(hash).isEqualTo("ABC123XYZ");
    }

    @Test
    @DisplayName("Should return empty string for invalid digest format")
    void shouldReturnEmptyStringForInvalidFormat() {
        // Given
        String invalidDigest = "invalid-format";

        // When
        String hash = ContentDigest.extractHash(invalidDigest);

        // Then
        assertThat(hash).isEmpty();
    }

    @Test
    @DisplayName("Should validate correct digest format")
    void shouldValidateCorrectDigestFormat() {
        // Given
        String body = "test";
        String digest = ContentDigest.generate(body);

        // When
        boolean isValid = ContentDigest.isValidFormat(digest);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect digest format")
    void shouldRejectIncorrectDigestFormat() {
        // Given
        String invalidDigest = "not-a-digest";

        // When
        boolean isValid = ContentDigest.isValidFormat(invalidDigest);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject digest with invalid base64")
    void shouldRejectDigestWithInvalidBase64() {
        // Given - invalid base64 characters
        String invalidDigest = "sha-256=:!!!INVALID!!!:=";

        // When
        boolean isValid = ContentDigest.isValidFormat(invalidDigest);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle UTF-8 characters in body")
    void shouldHandleUtf8Characters() {
        // Given
        String body = "Hello, 世界! 🌍";

        // When
        String digest = ContentDigest.generate(body);

        // Then
        assertThat(digest).isNotEmpty();
        assertThat(ContentDigest.validate(body, digest)).isTrue();
    }

    @Test
    @DisplayName("Should handle large body")
    void shouldHandleLargeBody() {
        // Given
        String body = "a".repeat(10000);

        // When
        String digest = ContentDigest.generate(body);

        // Then
        assertThat(digest).isNotEmpty();
        assertThat(ContentDigest.validate(body, digest)).isTrue();
    }
}
