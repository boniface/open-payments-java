plugins {
    id("org.sonarqube")
}

// SonarQube Configuration
afterEvaluate {
    sonar {
        properties {
            property("sonar.projectKey", "hashcode_open-payments-java")
            property("sonar.organization", "hashcode")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.sources", "src/main/java")
            property("sonar.tests", "src/test/java")
            property("sonar.java.binaries", "${layout.buildDirectory.get()}/classes/java/main")
            property("sonar.java.libraries", project.configurations.getByName("compileClasspath").files.joinToString(","))
            property("sonar.java.test.binaries", "${layout.buildDirectory.get()}/classes/java/test")
            property("sonar.java.test.libraries", project.configurations.getByName("testCompileClasspath").files.joinToString(","))
            property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.coverage.exclusions", "**/package-info.java,**/module-info.java")
            property("sonar.exclusions", "**/generated/**")
        }
    }
}
