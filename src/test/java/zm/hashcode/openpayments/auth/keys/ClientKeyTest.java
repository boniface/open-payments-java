package zm.hashcode.openpayments.auth.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClientKey}.
 */
class ClientKeyTest {

    private ClientKey clientKey;

    @BeforeEach
    void setUp() {
        clientKey = ClientKeyGenerator.generate("test-key-1");
    }

    @Test
    @DisplayName("Should create client key with valid key pair")
    void shouldCreateClientKeyWithValidKeyPair() {
        assertThat(clientKey.keyId()).isEqualTo("test-key-1");
        assertThat(clientKey.privateKey()).isNotNull();
        assertThat(clientKey.publicKey()).isNotNull();
        assertThat(clientKey.privateKey().getAlgorithm()).isIn("Ed25519", "EdDSA");
        assertThat(clientKey.publicKey().getAlgorithm()).isIn("Ed25519", "EdDSA");
    }

    @Test
    @DisplayName("Should create JWK from client key")
    void shouldCreateJwkFromClientKey() {
        // When
        JsonWebKey jwk = clientKey.toJwk();

        // Then
        assertThat(jwk.kid()).isEqualTo("test-key-1");
        assertThat(jwk.alg()).isEqualTo(JsonWebKey.ALGORITHM_EDDSA);
        assertThat(jwk.kty()).isEqualTo(JsonWebKey.KEY_TYPE_OKP);
        assertThat(jwk.crv()).isEqualTo(JsonWebKey.CURVE_ED25519);
        assertThat(jwk.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should sign data successfully")
    void shouldSignDataSuccessfully() {
        // Given
        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);

        // When
        byte[] signature = clientKey.sign(data);

        // Then
        assertThat(signature).isNotNull();
        assertThat(signature).hasSize(64); // Ed25519 signatures are 64 bytes
    }

    @Test
    @DisplayName("Should verify signature successfully")
    void shouldVerifySignatureSuccessfully() {
        // Given
        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        byte[] signature = clientKey.sign(data);

        // When
        boolean isValid = clientKey.verify(data, signature);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid signature")
    void shouldRejectInvalidSignature() {
        // Given
        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        byte[] wrongData = "Wrong data".getBytes(StandardCharsets.UTF_8);
        byte[] signature = clientKey.sign(data);

        // When
        boolean isValid = clientKey.verify(wrongData, signature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject tampered signature")
    void shouldRejectTamperedSignature() {
        // Given
        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        byte[] signature = clientKey.sign(data);

        // Tamper with signature
        signature[0] ^= 0x01;

        // When
        boolean isValid = clientKey.verify(data, signature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should produce consistent signatures for same data")
    void shouldProduceConsistentSignaturesForSameData() {
        // Given
        byte[] data = "Consistent data".getBytes(StandardCharsets.UTF_8);

        // When
        byte[] signature1 = clientKey.sign(data);
        byte[] signature2 = clientKey.sign(data);

        // Then - Ed25519 is deterministic, so signatures should be identical
        assertThat(signature1).isEqualTo(signature2);
    }

    @Test
    @DisplayName("Should throw exception when signing null data")
    void shouldThrowExceptionWhenSigningNullData() {
        assertThatThrownBy(() -> clientKey.sign(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data must not be null");
    }

    @Test
    @DisplayName("Should throw exception when verifying with null data")
    void shouldThrowExceptionWhenVerifyingWithNullData() {
        byte[] signature = new byte[64];
        assertThatThrownBy(() -> clientKey.verify(null, signature)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data must not be null");
    }

    @Test
    @DisplayName("Should throw exception when verifying with null signature")
    void shouldThrowExceptionWhenVerifyingWithNullSignature() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> clientKey.verify(data, null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("signatureBytes must not be null");
    }

    @Test
    @DisplayName("Should throw exception for null keyId")
    void shouldThrowExceptionForNullKeyId() {
        assertThatThrownBy(() -> new ClientKey(null, clientKey.privateKey(), clientKey.publicKey()))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("keyId must not be null");
    }

    @Test
    @DisplayName("Should throw exception for blank keyId")
    void shouldThrowExceptionForBlankKeyId() {
        assertThatThrownBy(() -> new ClientKey("", clientKey.privateKey(), clientKey.publicKey()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("keyId must not be blank");
    }

    @Test
    @DisplayName("Should throw exception for null private key")
    void shouldThrowExceptionForNullPrivateKey() {
        assertThatThrownBy(() -> new ClientKey("test", null, clientKey.publicKey()))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("privateKey must not be null");
    }

    @Test
    @DisplayName("Should throw exception for null public key")
    void shouldThrowExceptionForNullPublicKey() {
        assertThatThrownBy(() -> new ClientKey("test", clientKey.privateKey(), null))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("publicKey must not be null");
    }

    @Test
    @DisplayName("Should have toString without private key")
    void shouldHaveToStringWithoutPrivateKey() {
        // When
        String str = clientKey.toString();

        // Then
        assertThat(str).contains("test-key-1");
        assertThat(str).contains("Ed25519");
        assertThat(str).doesNotContain("privateKey"); // Should not expose private key
    }

    @Test
    @DisplayName("Should equals compare keyId and public key only")
    void shouldEqualsCompareKeyIdAndPublicKeyOnly() {
        // Given
        ClientKey key1 = ClientKeyGenerator.generate("same-key");
        ClientKey key2 = ClientKeyGenerator.generate("same-key");

        // When/Then - Different keys with same keyId should not be equal
        assertThat(key1).isNotEqualTo(key2);

        // Same key should equal itself
        assertThat(key1).isEqualTo(key1);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {
        // Given
        ClientKey key1 = clientKey;
        ClientKey key2 = new ClientKey(clientKey.keyId(), clientKey.privateKey(), clientKey.publicKey());

        // When/Then
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("Should sign and verify large data")
    void shouldSignAndVerifyLargeData() {
        // Given
        byte[] largeData = "a".repeat(100000).getBytes(StandardCharsets.UTF_8);

        // When
        byte[] signature = clientKey.sign(largeData);
        boolean isValid = clientKey.verify(largeData, signature);

        // Then
        assertThat(signature).isNotNull();
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should sign and verify empty data")
    void shouldSignAndVerifyEmptyData() {
        // Given
        byte[] emptyData = new byte[0];

        // When
        byte[] signature = clientKey.sign(emptyData);
        boolean isValid = clientKey.verify(emptyData, signature);

        // Then
        assertThat(signature).isNotNull();
        assertThat(isValid).isTrue();
    }
}
