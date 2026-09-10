plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.bundle.hybrid"
}

dependencies {
    api(project(":core"))
    api(project(":backend:firebase"))
    api(project(":backend:supabase"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
