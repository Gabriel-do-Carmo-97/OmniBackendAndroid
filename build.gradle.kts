// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.spotless)
}

apiValidation {
    ignoredProjects.addAll(listOf("app", "testing"))
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.4.1").editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "standard:function-naming" to "disabled",
                "standard:no-wildcard-imports" to "disabled",
            ),
        )
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.4.1")
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    afterEvaluate {
        extensions.findByName("detekt")?.let {
            val detektExt = it as? io.gitlab.arturbosch.detekt.extensions.DetektExtension
            detektExt?.buildUponDefaultConfig = true
            detektExt?.config?.setFrom(files("${rootProject.rootDir}/config/detekt/detekt.yml"))
            detektExt?.ignoreFailures = true
        }

        tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            jvmTarget = "11"
        }
    }
}
