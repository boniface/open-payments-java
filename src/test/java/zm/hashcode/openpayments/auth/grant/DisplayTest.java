package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link Display}.
 */
@DisplayName("Display")
class DisplayTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create with name only")
        void shouldCreateWithNameOnly() {
            Display display = Display.of("My App");

            assertThat(display.name()).isEqualTo("My App");
            assertThat(display.uri()).isEmpty();
        }

        @Test
        @DisplayName("should create with name and uri")
        void shouldCreateWithNameAndUri() {
            Display display = Display.of("My App", "https://example.com");

            assertThat(display.name()).isEqualTo("My App");
            assertThat(display.uri()).contains("https://example.com");
        }
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should throw when name is null")
        void shouldThrowWhenNameIsNull() {
            assertThatThrownBy(() -> new Display(null, java.util.Optional.empty()))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("name must not be null");
        }

        @Test
        @DisplayName("should handle null uri")
        void shouldHandleNullUri() {
            Display display = new Display("Test App", null);

            assertThat(display.name()).isEqualTo("Test App");
            assertThat(display.uri()).isEmpty();
        }
    }

    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerializationTests {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());

        @Test
        @DisplayName("should serialize with name only")
        void shouldSerializeWithNameOnly() throws Exception {
            Display display = Display.of("My Application");

            String json = objectMapper.writeValueAsString(display);

            assertThat(json).contains("\"name\"");
            assertThat(json).contains("\"My Application\"");
            assertThat(json).doesNotContain("\"uri\"");
        }

        @Test
        @DisplayName("should serialize with name and uri")
        void shouldSerializeWithNameAndUri() throws Exception {
            Display display = Display.of("My App", "https://example.com");

            String json = objectMapper.writeValueAsString(display);

            assertThat(json).contains("\"name\"");
            assertThat(json).contains("\"uri\"");
            assertThat(json).contains("https://example.com");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "name": "Test Application",
                        "uri": "https://test.example.com"
                    }
                    """;

            Display display = objectMapper.readValue(json, Display.class);

            assertThat(display.name()).isEqualTo("Test Application");
            assertThat(display.uri()).contains("https://test.example.com");
        }

        @Test
        @DisplayName("should deserialize with missing uri")
        void shouldDeserializeWithMissingUri() throws Exception {
            String json = """
                    {
                        "name": "Simple App"
                    }
                    """;

            Display display = objectMapper.readValue(json, Display.class);

            assertThat(display.name()).isEqualTo("Simple App");
            assertThat(display.uri()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty name")
        void shouldHandleEmptyName() {
            Display display = Display.of("");

            assertThat(display.name()).isEmpty();
        }

        @Test
        @DisplayName("should handle long name")
        void shouldHandleLongName() {
            String longName = "A".repeat(1000);
            Display display = Display.of(longName);

            assertThat(display.name()).hasSize(1000);
        }

        @Test
        @DisplayName("should handle special characters in name")
        void shouldHandleSpecialCharacters() {
            Display display = Display.of("My App™ © 2024");

            assertThat(display.name()).isEqualTo("My App™ © 2024");
        }
    }
}
