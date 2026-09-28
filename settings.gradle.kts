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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android\\.arch.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Gabriel-do-Carmo-97/OmniBackendAndroid")
            credentials {
                username =
                    providers.gradleProperty("gpr.user").orNull ?: providers.environmentVariable("GITHUB_ACTOR").orNull
                password =
                    providers.gradleProperty("gpr.key").orNull ?: providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}

rootProject.name = "OmniBackendAndroid"
include(":app")
include(":core")
include(":testing")

buildCache {
    local {
        directory = file("$rootDir/.gradle/build-cache")
    }
}

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
registerBundle("enterprise-hybrid")
registerBundle("edge-serverless")
registerBundle("baas-classic")
registerBundle("all")

// 🔗 Bundles de Pares Diretos Pré-configurados
registerBundle("firebase-supabase")
registerBundle("firebase-amplify")
registerBundle("firebase-back4app")
registerBundle("supabase-cloudflare")
