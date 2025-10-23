import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    jacoco
}

// JaCoCo Configuration - Code Coverage
jacoco {
    toolVersion = "0.8.13"
}

tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.withType<Test>())

    // Disable during development phase (interfaces only, no implementation yet)
    isEnabled = false

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/package-info.class",
                        "**/module-info.class",
                    )
                }
            },
        ),
    )
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    // Disable coverage verification during development
    isEnabled = false

    violationRules {
        rule {
            limit {
                minimum = "0.00".toBigDecimal() // 0% for development
            }
        }

        rule {
            element = "CLASS"
            limit {
                minimum = "0.00".toBigDecimal() // 0% for development
            }
            excludes =
                listOf(
                    "*.package-info",
                    "*.module-info",
                )
        }
    }
}
