package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link Client}.
 */
@DisplayName("Client")
class ClientTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should build with key")
        void shouldBuildWithKey() {
            Client client = Client.builder().key("https://example.com/jwks.json").build();

            assertThat(client.key()).contains("https://example.com/jwks.json");
            assertThat(client.display()).isEmpty();
        }

        @Test
        @DisplayName("should build with display")
        void shouldBuildWithDisplay() {
            Display display = Display.of("Test App");

            Client client = Client.builder().display(display).build();

            assertThat(client.display()).contains(display);
            assertThat(client.key()).isEmpty();
        }

        @Test
        @DisplayName("should build with both key and display")
        void shouldBuildWithKeyAndDisplay() {
            Display display = Display.of("My App", "https://myapp.com");

            Client client = Client.builder().key("https://example.com/jwks.json").display(display).build();

            assertThat(client.key()).contains("https://example.com/jwks.json");
            assertThat(client.display()).contains(display);
        }

        @Test
        @DisplayName("should build with no fields")
        void shouldBuildWithNoFields() {
            Client client = Client.builder().build();

            assertThat(client.key()).isEmpty();
            assertThat(client.display()).isEmpty();
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
            Client client = Client.builder().key("https://example.com/jwks.json").display(Display.of("Test App"))
                    .build();

            String json = objectMapper.writeValueAsString(client);

            assertThat(json).contains("\"key\"");
            assertThat(json).contains("\"display\"");
            assertThat(json).contains("https://example.com/jwks.json");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "key": "https://example.com/jwks.json",
                        "display": {
                            "name": "My Application"
                        }
                    }
                    """;

            Client client = objectMapper.readValue(json, Client.class);

            assertThat(client.key()).contains("https://example.com/jwks.json");
            assertThat(client.display()).isPresent();
            assertThat(client.display().get().name()).isEqualTo("My Application");
        }

        @Test
        @DisplayName("should not include absent fields")
        void shouldNotIncludeAbsentFields() throws Exception {
            Client client = Client.builder().key("https://example.com/jwks.json").build();

            String json = objectMapper.writeValueAsString(client);

            assertThat(json).contains("\"key\"");
            assertThat(json).doesNotContain("\"display\"");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should support fluent builder")
        void shouldSupportFluentBuilder() {
            Client client = Client.builder().key("https://example.com/jwks.json")
                    .display(Display.of("Test App", "https://test.com")).build();

            assertThat(client).isNotNull();
            assertThat(client.key()).isPresent();
            assertThat(client.display()).isPresent();
        }

        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            Client client = Client.builder().key(null).display(null).build();

            assertThat(client.key()).isEmpty();
            assertThat(client.display()).isEmpty();
        }

        @Test
        @DisplayName("should allow overwriting values")
        void shouldAllowOverwritingValues() {
            Client client = Client.builder().key("https://old.example.com/jwks.json")
                    .key("https://new.example.com/jwks.json").build();

            assertThat(client.key()).contains("https://new.example.com/jwks.json");
        }
    }
}
