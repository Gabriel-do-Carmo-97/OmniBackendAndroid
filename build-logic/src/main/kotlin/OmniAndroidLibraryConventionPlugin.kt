import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

class OmniAndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("maven-publish")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.consumerProguardFiles("consumer-rules.pro")

                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
            }

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    publications {
                        register<MavenPublication>("release") {
                            from(components["release"])
                            groupId = "br.wgc.omnibackend"
                            artifactId = target.name
                            version = target.version.toString().takeIf { it != "unspecified" } ?: "1.0.0"
                        }
                    }
                }
            }
        }
    }
}

