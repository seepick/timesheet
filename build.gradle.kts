repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    mavenLocal()
}

plugins {
    kotlin("jvm") version "2.2.20"
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("io.kotest:kotest-runner-junit5-jvm:6.0.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:6.0.3")
    testImplementation("io.mockk:mockk:1.14.6")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
