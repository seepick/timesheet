plugins {
    kotlin("jvm") version "2.2.20"
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("io.kotest:kotest-runner-junit5-jvm:6.0.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:6.0.3")
    testImplementation("io.mockk:mockk:1.14.6")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
