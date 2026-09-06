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
                            version = target.findProperty("version")?.toString()?.takeIf { it != "unspecified" }
                                ?: target.version.toString().takeIf { it != "unspecified" }
                                ?: "1.0.0"

                            pom {
                                name.set(target.name)
                                description.set("OmniBackend Android - ${target.name} module")
                                url.set("https://github.com/Gabriel-do-Carmo-97/OmniBackendAndroid")
                                licenses {
                                    license {
                                        name.set("The Apache License, Version 2.0")
                                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                    }
                                }
                                developers {
                                    developer {
                                        id.set("Gabriel-do-Carmo-97")
                                        name.set("Gabriel do Carmo")
                                    }
                                }
                                scm {
                                    connection.set("scm:git:git://github.com/Gabriel-do-Carmo-97/OmniBackendAndroid.git")
                                    developerConnection.set("scm:git:ssh://github.com:Gabriel-do-Carmo-97/OmniBackendAndroid.git")
                                    url.set("https://github.com/Gabriel-do-Carmo-97/OmniBackendAndroid")
                                }
                            }
                        }
                    }

                    repositories {
                        maven {
                            name = "GitHubPackages"
                            url = uri("https://maven.pkg.github.com/Gabriel-do-Carmo-97/OmniBackendAndroid")
                            credentials {
                                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                                    ?: findProperty("gpr.user") as? String
                                    ?: ""
                                password = providers.environmentVariable("GITHUB_TOKEN").orNull
                                    ?: findProperty("gpr.key") as? String
                                    ?: ""
                            }
                        }
                    }
                }
            }
        }
    }
}

