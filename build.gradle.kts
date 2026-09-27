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
    alias(libs.plugins.sonarqube) apply false
    alias(libs.plugins.dependency.check) apply false
    id("jacoco")
}

apiValidation {
    ignoredProjects.addAll(listOf("app", "testing"))
}

spotless {
    lineEndings = com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
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

jacoco {
    toolVersion = "0.8.12"
}

val jacocoExcludes =
    listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/androidx/**/*.*",
        "**/*\$Lambda\$*.*",
        "**/*\$Companion\$*.*",
        "**/*MembersInjector*.*",
        "**/*_MembersInjector.class",
        "**/*_Factory*.*",
        "**/*_Provide*Factory*.*",
        "**/*Hilt*.*",
        "**/Hilt_*.*",
        "**/*_HiltModules*.*",
        "**/di/*Module_*Factory*.*",
        "**/databinding/*.*",
    )

tasks.register<JacocoReport>("jacocoRootReport") {
    group = "Reporting"
    description = "Gera o relatório consolidado de cobertura Jacoco para todos os módulos."

    val targetProjects = subprojects.filter { it.name != "app" }

    dependsOn(targetProjects.map { it.tasks.matching { t -> t.name == "testDebugUnitTest" } })

    classDirectories.setFrom(
        targetProjects.map { sub ->
            fileTree(sub.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(jacocoExcludes)
            }
        },
    )

    sourceDirectories.setFrom(
        files(targetProjects.map { sub -> "${sub.projectDir}/src/main/java" }),
    )

    executionData.setFrom(
        targetProjects.map { sub ->
            fileTree(sub.layout.buildDirectory.dir("outputs/unit_test_code_coverage/debugUnitTest")) {
                include("**/*.exec")
            }
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoRootReport/jacocoRootReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoRootReport/html"))
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
            val projectBaseline = file("$projectDir/detekt-baseline.xml")
            if (projectBaseline.exists()) {
                detektExt?.baseline = projectBaseline
            } else {
                val baselineFile = file("${rootProject.rootDir}/config/detekt/baseline.xml")
                if (baselineFile.exists()) {
                    detektExt?.baseline = baselineFile
                }
            }
            detektExt?.ignoreFailures = false
        }

        tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            jvmTarget = "11"
        }
    }
}
