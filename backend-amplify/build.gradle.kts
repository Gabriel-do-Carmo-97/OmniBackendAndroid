plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.amplify"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.javax.inject)

    // Ktor Client & Gson para AWS Amplify REST/AppSync/Cognito API
    implementation(libs.ktor.client.android)
    implementation(libs.google.gson)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

