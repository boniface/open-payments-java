import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.KotlinClosure2

plugins {
    java
}

tasks {
    test {
        useJUnitPlatform {
            excludeTags("integration")
        }
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = false
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    println("\n═══════════════════════════════════════════════════════════")
                    println("  Test Results")
                    println("═══════════════════════════════════════════════════════════")
                    println("  Total:   ${result.testCount}")
                    println("  Passed:  ${result.successfulTestCount}")
                    println("  Failed:  ${result.failedTestCount}")
                    println("  Skipped: ${result.skippedTestCount}")
                    println("───────────────────────────────────────────────────────────")
                    val resultText =
                        when {
                            result.failedTestCount > 0 -> "FAILED"
                            result.skippedTestCount > 0 -> "SUCCESS (with skipped)"
                            else -> "SUCCESS"
                        }
                    println("  Result:  $resultText")
                    println("═══════════════════════════════════════════════════════════\n")
                }
            }),
        )

        doLast {
            if (state.skipped) {
                println("\n═══════════════════════════════════════════════════════════")
                println("  Test Results (from cache)")
                println("═══════════════════════════════════════════════════════════")
                println("  Tests were not executed - results are up-to-date")
                println("  Run with --rerun-tasks to force execution and see details")
                println("═══════════════════════════════════════════════════════════\n")
            }
        }
    }

    val integrationTest by registering(Test::class) {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform {
            includeTags("integration")
        }

        shouldRunAfter(test)
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true // Show output for integration tests
        }

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    println("\n═══════════════════════════════════════════════════════════")
                    println("  Integration Test Results")
                    println("═══════════════════════════════════════════════════════════")
                    println("  Total:   ${result.testCount}")
                    println("  Passed:  ${result.successfulTestCount}")
                    println("  Failed:  ${result.failedTestCount}")
                    println("  Skipped: ${result.skippedTestCount}")
                    println("───────────────────────────────────────────────────────────")
                    val resultText =
                        when {
                            result.failedTestCount > 0 -> "FAILED"
                            result.skippedTestCount > 0 -> "SUCCESS (with skipped)"
                            else -> "SUCCESS"
                        }
                    println("  Result:  $resultText")
                    println("═══════════════════════════════════════════════════════════\n")
                }
            }),
        )

        doLast {
            if (state.skipped) {
                println("\n═══════════════════════════════════════════════════════════")
                println("  Integration Test Results (from cache)")
                println("═══════════════════════════════════════════════════════════")
                println("  Tests were not executed - results are up-to-date")
                println("  Run with --rerun-tasks to force execution and see details")
                println("═══════════════════════════════════════════════════════════\n")
            }
        }
    }

    val allTests by registering(Test::class) {
        description = "Runs all tests (unit and integration)."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform()
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    println("\n═══════════════════════════════════════════════════════════")
                    println("  All Tests Results (Unit + Integration)")
                    println("═══════════════════════════════════════════════════════════")
                    println("  Total:   ${result.testCount}")
                    println("  Passed:  ${result.successfulTestCount}")
                    println("  Failed:  ${result.failedTestCount}")
                    println("  Skipped: ${result.skippedTestCount}")
                    println("───────────────────────────────────────────────────────────")
                    val resultText =
                        when {
                            result.failedTestCount > 0 -> "FAILED"
                            result.skippedTestCount > 0 -> "SUCCESS (with skipped)"
                            else -> "SUCCESS"
                        }
                    println("  Result:  $resultText")
                    println("═══════════════════════════════════════════════════════════\n")
                }
            }),
        )

        doLast {
            if (state.skipped) {
                println("\n═══════════════════════════════════════════════════════════")
                println("  All Tests Results (from cache)")
                println("═══════════════════════════════════════════════════════════")
                println("  Tests were not executed - results are up-to-date")
                println("  Run with --rerun-tasks to force execution and see details")
                println("═══════════════════════════════════════════════════════════\n")
            }
        }
    }

    register("testReport") {
        group = "verification"
        description = "Runs all tests and always shows detailed results"
        dependsOn(allTests)
        doLast {
            println("\n═══════════════════════════════════════════════════════════")
            println("  ℹ️  Test Report")
            println("═══════════════════════════════════════════════════════════")
            println("  Detailed test results shown above.")
            println("  To force re-execution: ./gradlew allTests --rerun-tasks")
            println("  Test report: build/reports/tests/allTests/index.html")
            println("═══════════════════════════════════════════════════════════\n")
        }
    }
}
