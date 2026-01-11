plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the detekt static analysis plugin for Kotlin code.
    // See: https://detekt.dev/docs/1.23.8/gettingstarted/gradle
    id("io.gitlab.arturbosch.detekt") version "1.23.8"

    // Apply Dokka for automatic documentation generation
    id("org.jetbrains.dokka") version "2.1.0"

    // Apply the application plugin to add support for building a CLI application in Java.
    application

    // Apply the JaCoCo plugin for code coverage reports.
    jacoco
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

    // GitHub Packages repository for aws-data-deletion-sdk
    maven {
        url = uri("https://maven.pkg.github.com/On-Demand-Deletion-Platform/aws-data-deletion-sdk")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // AWS Data Deletion SDK
    implementation("com.ondemanddeletionplatform:aws-data-deletion-sdk:0.0.2")
    
    // AWS SDK for Kotlin
    implementation("aws.sdk.kotlin:dynamodb:1.5.113")

    // JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito Kotlin for mocking in tests.
    // See: https://github.com/mockito/mockito-kotlin/wiki/Mocking-and-verifying
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.1.0")

    // Testcontainers for local integration testing.
    // See: https://java.testcontainers.org/quickstart/junit_5_quickstart/
    testImplementation("org.testcontainers:localstack:1.21.4")
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.3")

    // Detekt dependencies for static code analysis.
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.ondemanddeletionplatform.deletionworker.DeletionWorkerKt")
}

jacoco {
    toolVersion = "0.8.10"
}

detekt {
    // Version of detekt to use. For a list of available versions,
    // see https://github.com/detekt/detekt/releases.
    toolVersion = "1.23.8"

    // Directory where detekt will search for source files.
    source.setFrom("src/main/kotlin", "src/test/kotlin", "src/localIntegTest/kotlin")

    // Specify custom detekt config file for overriding lint rules.
    config.setFrom("$projectDir/config/detekt.yml")

    // Builds the AST in parallel, can speed up builds in larger projects.
    parallel = true

    // Adds debug output during task execution. `false` by default.
    debug = false
}

tasks.test {
    useJUnitPlatform {
        excludeTags("localIntegTest")
    }

    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "21"

    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "21"
}

// ------------------------------
// Local integration test config
// ------------------------------

// Define source set for local integ tests
val localIntegTestSourceSet = sourceSets.create("localIntegTest") {
    kotlin.srcDir("src/localIntegTest/kotlin")
    resources.srcDir("src/localIntegTest/resources")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

// Reuse test dependencies for local integ tests
configurations["localIntegTestImplementation"]
    .extendsFrom(configurations["testImplementation"])
configurations["localIntegTestRuntimeOnly"]
    .extendsFrom(configurations["testRuntimeOnly"])

// Define task to run local integ tests
val localIntegTest = tasks.register<Test>("localIntegTest") {
    description = "Runs local integration tests."
    group = "verification"

    testClassesDirs = localIntegTestSourceSet.output.classesDirs
    classpath = localIntegTestSourceSet.runtimeClasspath

    useJUnitPlatform {
        includeTags("localIntegTest")
    }

    shouldRunAfter(tasks.test)

    onlyIf {
        System.getProperty("runLocalIntegTests") == "true"
    }
}

tasks.build {
    // Ensure localIntegTest compilation is included in build task
    dependsOn(tasks.named("compileLocalIntegTestKotlin"))

    // Auto-generate documentation during builds
    dependsOn(tasks.dokkaGenerateHtml)
}
