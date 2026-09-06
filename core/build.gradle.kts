plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.core"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
