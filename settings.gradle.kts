pluginManagement {
    repositories {
//        val gradleProp = File(System.getProperty("user.home"), "/.gradle/gradle.properties")
//        val azureToken = if (gradleProp.exists()) {
//            java.util.Properties().apply {
//                load(gradleProp.reader())
//            }["uwv.azure_token"]?.toString()
//        } else null
        val azureToken = System.getenv("azureToken")
        if (azureToken != null) {
            println("Using UWV plugin repo.")
            maven {
                name = "UWV artifacts"
                credentials {
                    username = "UWV"
                    password = azureToken
                }
                url = uri("https://azuredevops.ba.uwv.nl/UWV/_packaging/UWV/maven/v1")
                authentication.create<BasicAuthentication>("basic")
            }
        } else {
            println("Using default plugin repo.")
            google()
            gradlePluginPortal()
            mavenCentral()
        }
    }
}

rootProject.name = "timesheet"
