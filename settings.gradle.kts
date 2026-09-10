pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "OmniBackendAndroid"
include(":app")
include(":core")

// 🚀 Registra drivers especializados da pasta backend/
fun registerBackend(name: String) {
    include(":backend:$name")
    project(":backend:$name").projectDir = file("backend/$name")
}

registerBackend("firebase")
registerBackend("supabase")
registerBackend("appwrite")
registerBackend("pocketbase")
registerBackend("back4app")
registerBackend("amplify")
registerBackend("rest")
registerBackend("cloudflare")

// 📦 Registra bundles agregadores da pasta bundle/
fun registerBundle(name: String) {
    include(":bundle:$name")
    project(":bundle:$name").projectDir = file("bundle/$name")
}

registerBundle("hybrid")
registerBundle("self-hosted")
registerBundle("cloud-native")
registerBundle("all")