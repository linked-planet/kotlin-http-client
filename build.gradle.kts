plugins {
    id("pl.allegro.tech.build.axion-release") version "1.13.6"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.github.hierynomus.license") version "0.15.0"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlin.jvm") version "1.4.21" apply (false)
    id("org.jetbrains.dokka") version "1.6.10" apply (false)
}

val uiKitVersion: String = scmVersion.version
allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.atlassian.com/repository/public")
    }

    group = "com.linked-planet"
    version = uiKitVersion
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.hierynomus.license")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    license {
        header = rootProject.file("LICENSE-HEADER.txt")
        strictCheck = true

        exclude("**/*.properties")
        exclude("**/*.json")

        ext["year"] = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        ext["owner"] = "linked-planet GmbH"
    }

    tasks {
        register("javadocJar", Jar::class) {
            dependsOn("dokkaJavadoc")
            archiveClassifier.set("javadoc")
            from("$buildDir/dokka/javadoc")
        }
        register("sourcesJar", Jar::class) {
            archiveClassifier.set("sources")
            from("src/main/kotlin")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(tasks.getByName<Zip>("javadocJar"))
                artifact(tasks.getByName<Zip>("sourcesJar"))

                pom {
                    name.set(artifactId)
                    description.set("Provides a http-client for kotlin atlassian libraries.")
                    url.set("https://github.com/linked-planet/kotlin-http-client")
                    inceptionYear.set("2022")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            name.set("Philipp Karlsson")
                            email.set("philipp.karlsson@linked-planet.com")
                            url.set("https://github.com/betacore")
                            organization.set("linked-planet GmbH")
                            organizationUrl.set("https://linked-planet.com")
                        }
                        developer {
                            name.set("Simon Jahreiss")
                            email.set("simon.jahreiss@linked-planet.com")
                            url.set("https://github.com/sjahreis")
                            organization.set("linked-planet GmbH")
                            organizationUrl.set("https://linked-planet.com")
                        }
                        developer {
                            name.set("Alexander Weickmann")
                            email.set("alexander.weickmann@linked-planet.com")
                            url.set("https://github.com/weickmanna")
                            organization.set("linked-planet GmbH")
                            organizationUrl.set("https://linked-planet.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/linked-planet/kotlin-http-client.git")
                        developerConnection.set("scm:git:ssh://github.com/linked-planet/kotlin-http-client.git")
                        url.set("https://github.com/linked-planet/kotlin-http-client")
                    }
                }
            }
        }
    }

    signing {
        isRequired = !project.version.toString().endsWith("-SNAPSHOT") && !project.hasProperty("skipSigning")
        if (project.findProperty("signingKey") != null) {
            useInMemoryPgpKeys(
                findProperty("signingKey").toString(),
                findProperty("signingPassword").toString()
            )
        } else {
            useGpgCmd()
        }
        sign(publishing.publications["maven"])
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

// do not generate extra load on Nexus with new staging repository if signing fails
val initializeSonatypeStagingRepository by tasks.existing
subprojects {
    initializeSonatypeStagingRepository {
        shouldRunAfter(tasks.withType<Sign>())
    }
}
