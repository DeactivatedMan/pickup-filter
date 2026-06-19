import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("com.github.benmanes.caffeine", "me.deac.pickupFilter.libs.caffeine")
    archiveClassifier.set("")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks {
    runServer {
        minecraftVersion("1.21")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to props)
        }
    }
}