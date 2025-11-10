import java.util.*

//import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
//    val gradleProp = File(System.getProperty("user.home"), "/.gradle/gradle.properties")
//    val azureToken = if (gradleProp.exists()) {
//        Properties().apply {
//            load(gradleProp.reader())
//        }["uwv.azure_token"]?.toString()
//    } else null
//    if (azureToken != null) {
    println("Using UWV repo.")
    maven {
        name = "UWV artifacts"
        credentials {
            username = "UWV"
            password = "" // azureToken
        }
        url = uri("https://azuredevops.ba.uwv.nl/UWV/_packaging/UWV/maven/v1")
        authentication.create<BasicAuthentication>("basic")
    }
//    } else {
//        println("Using default repos.")
//        mavenCentral()
//        mavenLocal()
//    }
}

plugins {
    kotlin("jvm") version "2.1.0" // NO! "2.2.20"
//    id("com.github.ben-manes.versions") version "0.53.0"
}

dependencies {
    implementation(kotlin("stdlib"))

    listOf("runner-junit5-jvm", "assertions-core").forEach {
        testImplementation("io.kotest:kotest-$it:5.9.1") // NO! "6.0.4"
    }
    testImplementation("io.mockk:mockk:1.14.6")
}

//tasks.withType<Test>().configureEach {
tasks.test {
    useJUnitPlatform()
}

//tasks.withType<DependencyUpdatesTask> {
//    val rejectPatterns =
//        listOf(".*-ea.*", ".*RC.*", ".*rc.*", ".*M1", ".*check", ".*dev.*", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
//    rejectVersionIf {
//        rejectPatterns.any {
//            it.matches(candidate.version)
//        }
//    }
//}
