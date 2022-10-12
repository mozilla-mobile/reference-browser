// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        if (project.hasProperty("googleRepo")) {
            maven {
                name "Google"
                url project.property("googleRepo")
                allowInsecureProtocol true // Local Nexus in CI uses HTTP
            }
        } else {
            google()
        }

        if (project.hasProperty("centralRepo")) {
            maven {
                name "MavenCentral"
                url project.property("centralRepo")
                allowInsecureProtocol true // Local Nexus in CI uses HTTP
            }
        } else {
            mavenCentral()
        }
    }

    dependencies {
        classpath Deps.tools_androidgradle
        classpath Deps.tools_kotlingradle
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.19.0" // Variables in plugins {} aren"t supported
}

allprojects {
    repositories {
        if (project.hasProperty("googleRepo")) {
            maven {
                name "Google"
                url project.property("googleRepo")
                allowInsecureProtocol true // Local Nexus in CI uses HTTP
            }
        } else {
            google()
        }

        maven {
            name "Mozilla Nightly"
            url "https://nightly.maven.mozilla.org/maven2"

            content {
                // Always fetch components from the snapshots repository
                includeGroup "org.mozilla.components"
            }
        }

        maven {
            name "Mozilla"
            url "https://maven.mozilla.org/maven2"

            content {
                // Never fetch components from here. We always want to use snapshots.
                excludeGroup "org.mozilla.components"
            }
        }

        if (project.hasProperty("centralRepo")) {
            maven {
                name "MavenCentral"
                url project.property("centralRepo")
                allowInsecureProtocol true // Local Nexus in CI uses HTTP
            }
        } else {
            mavenCentral()
        }
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
        kotlinOptions.freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

subprojects {
    apply(plugin = "jacoco")

    afterEvaluate {
        if (it.hasProperty("android")) {
            jacoco {
                toolVersion = Versions.jacoco_version
            }

            android {
                testOptions {
                    unitTests {
                        includeAndroidResources = true
                    }
                }

                lintOptions {
                    warningsAsErrors true
                    isAbortOnError = true
                }
            }

            if (project.hasProperty("coverage") && project.name != "support-test") {
                tasks.withType(Test) {
                    jacoco.includeNoLocationClasses = true
                    doLast { jacocoTestReport.execute() }
                }

                task jacocoTestReport(type: JacocoReport) {
                    reports {
                        xml.enabled = true
                        html.enabled = true
                    }

                    val fileFilter = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
                                      "**/*Test*.*", "android/**/*.*", "**/*$[0-9).*"]
                    val kotlinDebugTree = fileTree(dir = "$project.buildDir/tmp/kotlin-classes/debug", excludes: fileFilter)
                    val javaDebugTree = fileTree(dir = "$project.buildDir/intermediates/classes/debug", excludes: fileFilter)
                    val mainSrc = "$project.projectDir/src/main/java"

                    sourceDirectories = files(listOf(mainSrc))
                    classDirectories = files(listOf(kotlinDebugTree, javaDebugTree))
                    executionData = fileTree(dir: project.buildDir, includes: listOf(
                            "jacoco/testDebugUnitTest.exec", "outputs/code-coverage/connected/*coverage.ec"
                    ))
                }

                android {
                    buildTypes {
                        named("debug") {
                            testCoverageEnabled true
                        }
                    }
                }
            }
        }
    }
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
