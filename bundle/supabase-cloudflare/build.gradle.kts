plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.bundle.supabasecloudflare"
}

dependencies {
    api(project(":core"))
    api(project(":bundle:hybrid"))
    api(project(":backend:supabase"))
    api(project(":backend:cloudflare"))

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
