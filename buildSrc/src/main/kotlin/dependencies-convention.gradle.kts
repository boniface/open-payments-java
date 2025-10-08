plugins {
    `java-library`
}

val httpClient5Version = "5.5.1"
val okhttpVersion = "5.1.0"
val jacksonVersion = "2.20.0"
val httpSignaturesVersion = "1.8"
val jakartaValidationVersion = "3.1.1"
val hibernateValidatorVersion = "9.0.1.Final"
val slf4jVersion = "2.0.17"
val guavaVersion = "33.5.0-jre"
val junitVersion = "6.0.0"
val mockitoVersion = "5.20.0"
val assertjVersion = "3.27.6"
val mockWebServerVersion = "4.12.0"
val logbackVersion = "1.5.12"

dependencies {
    // HTTP Clients - Multiple implementations available
    implementation("org.apache.httpcomponents.client5:httpclient5:$httpClient5Version")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    // JSON Processing - Jackson for JSON serialization/deserialization
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")

    // HTTP Signatures - For Open Payments authentication
    implementation("org.tomitribe:tomitribe-http-signatures:$httpSignaturesVersion")

    // Validation
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")

    // Logging
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // Utilities
    implementation("com.google.guava:guava:$guavaVersion")

    // Testing
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$mockWebServerVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
