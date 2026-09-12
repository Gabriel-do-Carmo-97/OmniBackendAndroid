plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.bundle.firebaseamplify"
}

dependencies {
    api(project(":core"))
    api(project(":bundle:hybrid"))
    api(project(":backend:firebase"))
    api(project(":backend:amplify"))

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
