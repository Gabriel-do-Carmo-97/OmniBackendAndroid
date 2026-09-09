plugins {
    id("omni.android.library")
    alias(libs.plugins.dokka)
}

android {
    namespace = "br.wgc.omnibackend.bundle.all"
}

dependencies {
    api(project(":core"))
    api(project(":bundle:hybrid"))

    api(project(":backend:firebase"))
    api(project(":backend:supabase"))
    api(project(":backend:appwrite"))
    api(project(":backend:pocketbase"))
    api(project(":backend:back4app"))
    api(project(":backend:amplify"))
    api(project(":backend:rest"))
    api(project(":backend:cloudflare"))

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
