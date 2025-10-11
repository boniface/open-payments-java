package zm.hashcode.openpayments.auth.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.auth.exception.KeyException;

/**
 * Unit tests for {@link JsonWebKey}.
 */
class JsonWebKeyTest {

    @Test
    @DisplayName("Should create valid Ed25519 JWK")
    void shouldCreateValidEd25519Jwk() {
        // Given
        String kid = "test-key-1";
        String x = "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo"; // Valid base64url

        // When
        JsonWebKey jwk = JsonWebKey.builder().kid(kid).x(x).build();

        // Then
        assertThat(jwk.kid()).isEqualTo(kid);
        assertThat(jwk.alg()).isEqualTo(JsonWebKey.ALGORITHM_EDDSA);
        assertThat(jwk.kty()).isEqualTo(JsonWebKey.KEY_TYPE_OKP);
        assertThat(jwk.crv()).isEqualTo(JsonWebKey.CURVE_ED25519);
        assertThat(jwk.x()).isEqualTo(x);
        assertThat(jwk.use()).isEqualTo(Optional.of(JsonWebKey.USE_SIGNATURE));
    }

    @Test
    @DisplayName("Should validate correct Ed25519 JWK")
    void shouldValidateCorrectEd25519Jwk() {
        // Given
        JsonWebKey jwk = JsonWebKey.builder().kid("test-key").x("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo").build();

        // When
        boolean isValid = jwk.isValid();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should recognize Ed25519 signature key")
    void shouldRecognizeEd25519SignatureKey() {
        // Given
        JsonWebKey jwk = JsonWebKey.builder().kid("test-key").x("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo").build();

        // When
        boolean isSignatureKey = jwk.isEd25519SignatureKey();

        // Then
        assertThat(isSignatureKey).isTrue();
    }

    @Test
    @DisplayName("Should get public key bytes")
    void shouldGetPublicKeyBytes() {
        // Given
        String x = "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo";
        JsonWebKey jwk = JsonWebKey.builder().kid("test-key").x(x).build();

        // When
        byte[] publicKeyBytes = jwk.getPublicKeyBytes();

        // Then
        assertThat(publicKeyBytes).hasSize(32); // Ed25519 public keys are 32 bytes
    }

    @Test
    @DisplayName("Should throw exception for null kid")
    void shouldThrowExceptionForNullKid() {
        assertThatThrownBy(() -> new JsonWebKey(null, JsonWebKey.ALGORITHM_EDDSA, JsonWebKey.KEY_TYPE_OKP,
                JsonWebKey.CURVE_ED25519, "test", Optional.empty())).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("kid must not be null");
    }

    @Test
    @DisplayName("Should throw exception for blank kid")
    void shouldThrowExceptionForBlankKid() {
        assertThatThrownBy(() -> new JsonWebKey("", JsonWebKey.ALGORITHM_EDDSA, JsonWebKey.KEY_TYPE_OKP,
                JsonWebKey.CURVE_ED25519, "test", Optional.empty())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kid must not be blank");
    }

    @Test
    @DisplayName("Should throw exception for null alg")
    void shouldThrowExceptionForNullAlg() {
        assertThatThrownBy(() -> new JsonWebKey("kid", null, JsonWebKey.KEY_TYPE_OKP, JsonWebKey.CURVE_ED25519, "test",
                Optional.empty())).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("alg must not be null");
    }

    @Test
    @DisplayName("Should throw exception for null kty")
    void shouldThrowExceptionForNullKty() {
        assertThatThrownBy(() -> new JsonWebKey("kid", JsonWebKey.ALGORITHM_EDDSA, null, JsonWebKey.CURVE_ED25519,
                "test", Optional.empty())).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("kty must not be null");
    }

    @Test
    @DisplayName("Should throw exception for null crv")
    void shouldThrowExceptionForNullCrv() {
        assertThatThrownBy(() -> new JsonWebKey("kid", JsonWebKey.ALGORITHM_EDDSA, JsonWebKey.KEY_TYPE_OKP, null,
                "test", Optional.empty())).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("crv must not be null");
    }

    @Test
    @DisplayName("Should throw exception for null x")
    void shouldThrowExceptionForNullX() {
        assertThatThrownBy(() -> new JsonWebKey("kid", JsonWebKey.ALGORITHM_EDDSA, JsonWebKey.KEY_TYPE_OKP,
                JsonWebKey.CURVE_ED25519, null, Optional.empty())).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("x must not be null");
    }

    @Test
    @DisplayName("Should throw exception for blank x")
    void shouldThrowExceptionForBlankX() {
        assertThatThrownBy(() -> new JsonWebKey("kid", JsonWebKey.ALGORITHM_EDDSA, JsonWebKey.KEY_TYPE_OKP,
                JsonWebKey.CURVE_ED25519, "", Optional.empty())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("x must not be blank");
    }

    @Test
    @DisplayName("Should throw exception for invalid base64url in x")
    void shouldThrowExceptionForInvalidBase64UrlInX() {
        // Given
        JsonWebKey jwk = JsonWebKey.builder().kid("test-key").x("!!!INVALID!!!").build();

        // When/Then
        assertThatThrownBy(jwk::getPublicKeyBytes).isInstanceOf(KeyException.class)
                .hasMessageContaining("Invalid base64url encoding");
    }

    @Test
    @DisplayName("Should create JWK without use field")
    void shouldCreateJwkWithoutUseField() {
        // Given
        JsonWebKey jwk = JsonWebKey.builder().kid("test-key").x("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo").use(null)
                .build();

        // When
        Optional<String> use = jwk.use();

        // Then
        assertThat(use).isEmpty();
        assertThat(jwk.isEd25519SignatureKey()).isTrue(); // Should still be valid
    }

    @Test
    @DisplayName("Should create JWK from Ed25519 public key")
    void shouldCreateJwkFromEd25519PublicKey() {
        // Given
        ClientKey clientKey = ClientKeyGenerator.generate("test-key");

        // When
        JsonWebKey jwk = JsonWebKey.from("test-key", clientKey.publicKey());

        // Then
        assertThat(jwk.kid()).isEqualTo("test-key");
        assertThat(jwk.alg()).isEqualTo(JsonWebKey.ALGORITHM_EDDSA);
        assertThat(jwk.kty()).isEqualTo(JsonWebKey.KEY_TYPE_OKP);
        assertThat(jwk.crv()).isEqualTo(JsonWebKey.CURVE_ED25519);
        assertThat(jwk.x()).isNotEmpty();
        assertThat(jwk.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should reject wrong algorithm in JWK validation")
    void shouldRejectWrongAlgorithmInValidation() {
        // Given
        JsonWebKey jwk = new JsonWebKey("kid", "RS256", // Wrong algorithm
                JsonWebKey.KEY_TYPE_OKP, JsonWebKey.CURVE_ED25519, "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo",
                Optional.of(JsonWebKey.USE_SIGNATURE));

        // When
        boolean isValid = jwk.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject wrong key type in JWK validation")
    void shouldRejectWrongKeyTypeInValidation() {
        // Given
        JsonWebKey jwk = new JsonWebKey("kid", JsonWebKey.ALGORITHM_EDDSA, "RSA", // Wrong key type
                JsonWebKey.CURVE_ED25519, "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo",
                Optional.of(JsonWebKey.USE_SIGNATURE));

        // When
        boolean isValid = jwk.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject wrong curve in JWK validation")
    void shouldRejectWrongCurveInValidation() {
        // Given
        JsonWebKey jwk = new JsonWebKey("kid", JsonWebKey.ALGORITHM_EDDSA, JsonWebKey.KEY_TYPE_OKP, "P-256", // Wrong
                // curve
                "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo", Optional.of(JsonWebKey.USE_SIGNATURE));

        // When
        boolean isValid = jwk.isValid();

        // Then
        assertThat(isValid).isFalse();
    }
}
