plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.bundle.selfhosted"
}

dependencies {
    api(project(":core"))
    api(project(":bundle:hybrid"))
    api(project(":backend:pocketbase"))
    api(project(":backend:appwrite"))

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
