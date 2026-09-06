plugins {
    `kotlin-dsl`
}

group = "br.wgc.omnibackend.buildlogic"

kotlin {
    jvmToolchain(17)
}


dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "omni.android.application"
            implementationClass = "OmniAndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "omni.android.application.compose"
            implementationClass = "OmniAndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "omni.android.library"
            implementationClass = "OmniAndroidLibraryConventionPlugin"
        }
    }
}
