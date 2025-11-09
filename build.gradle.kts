import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.1.0" // NO! "2.2.20"
    id("com.github.ben-manes.versions") version "0.53.0"
}

dependencies {
    implementation(kotlin("stdlib"))

    listOf("runner-junit5-jvm", "assertions-core", "property").forEach {
        testImplementation("io.kotest:kotest-$it:5.9.1") // NO! "6.0.4"
    }

    testImplementation("io.mockk:mockk:1.14.6")
}

//tasks.withType<Test>().configureEach {
tasks.test {
    useJUnitPlatform()
}

tasks.withType<DependencyUpdatesTask> {
    val rejectPatterns =
        listOf(".*-ea.*", ".*RC.*", ".*rc.*", ".*M1", ".*check", ".*dev.*", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
    rejectVersionIf {
        rejectPatterns.any {
            it.matches(candidate.version)
        }
    }
}
