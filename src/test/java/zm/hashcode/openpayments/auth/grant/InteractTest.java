package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link Interact}.
 */
@DisplayName("Interact")
class InteractTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create redirect interaction")
        void shouldCreateRedirectInteraction() {
            Interact interact = Interact.redirect("https://example.com/callback", "nonce-12345");

            assertThat(interact.start()).containsExactly("redirect");
            assertThat(interact.finish()).isPresent();

            Finish finish = interact.finish().get();
            assertThat(finish.method()).isEqualTo("redirect");
            assertThat(finish.uri()).isEqualTo("https://example.com/callback");
            assertThat(finish.nonce()).isEqualTo("nonce-12345");
        }
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create with start only")
        void shouldCreateWithStartOnly() {
            Interact interact = new Interact(List.of("redirect"), java.util.Optional.empty());

            assertThat(interact.start()).containsExactly("redirect");
            assertThat(interact.finish()).isEmpty();
        }

        @Test
        @DisplayName("should create with start and finish")
        void shouldCreateWithStartAndFinish() {
            Finish finish = new Finish("redirect", "https://example.com/callback", "nonce");

            Interact interact = new Interact(List.of("redirect"), java.util.Optional.of(finish));

            assertThat(interact.start()).containsExactly("redirect");
            assertThat(interact.finish()).contains(finish);
        }

        @Test
        @DisplayName("should throw when start is null")
        void shouldThrowWhenStartIsNull() {
            assertThatThrownBy(() -> new Interact(null, java.util.Optional.empty()))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("start must not be null");
        }

        @Test
        @DisplayName("should throw when start is empty")
        void shouldThrowWhenStartIsEmpty() {
            assertThatThrownBy(() -> new Interact(List.of(), java.util.Optional.empty()))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("start must not be empty");
        }

        @Test
        @DisplayName("should handle null finish")
        void shouldHandleNullFinish() {
            Interact interact = new Interact(List.of("redirect"), null);

            assertThat(interact.finish()).isEmpty();
        }
    }

    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerializationTests {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());

        @Test
        @DisplayName("should serialize to JSON")
        void shouldSerializeToJson() throws Exception {
            Interact interact = Interact.redirect("https://example.com/callback", "nonce-abc");

            String json = objectMapper.writeValueAsString(interact);

            assertThat(json).contains("\"start\"");
            assertThat(json).contains("\"redirect\"");
            assertThat(json).contains("\"finish\"");
            assertThat(json).contains("https://example.com/callback");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "start": ["redirect"],
                        "finish": {
                            "method": "redirect",
                            "uri": "https://example.com/callback",
                            "nonce": "test-nonce"
                        }
                    }
                    """;

            Interact interact = objectMapper.readValue(json, Interact.class);

            assertThat(interact.start()).containsExactly("redirect");
            assertThat(interact.finish()).isPresent();
            assertThat(interact.finish().get().uri()).isEqualTo("https://example.com/callback");
        }

        @Test
        @DisplayName("should not include absent finish")
        void shouldNotIncludeAbsentFinish() throws Exception {
            Interact interact = new Interact(List.of("redirect"), java.util.Optional.empty());

            String json = objectMapper.writeValueAsString(interact);

            assertThat(json).contains("\"start\"");
            assertThat(json).doesNotContain("\"finish\"");
        }
    }

    @Nested
    @DisplayName("Multiple Start Methods")
    class MultipleStartMethodsTests {

        @Test
        @DisplayName("should support multiple start methods")
        void shouldSupportMultipleStartMethods() {
            Interact interact = new Interact(List.of("redirect", "app"), java.util.Optional.empty());

            assertThat(interact.start()).containsExactly("redirect", "app");
        }

        @Test
        @DisplayName("should maintain start method order")
        void shouldMaintainStartMethodOrder() {
            Interact interact = new Interact(List.of("app", "redirect", "user_code"), java.util.Optional.empty());

            assertThat(interact.start()).containsExactly("app", "redirect", "user_code");
        }
    }
}
