import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "xyz.mizarc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/central")
    }
    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.spigotmc:spigot-api:1.18-pre8-R0.1-SNAPSHOT")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("com.github.MilkBowl:VaultAPI:1.7")
}

tasks.shadowJar {
    relocate("co.aikar.commands", "xyz.mizarc.solidclaims.acf")
    relocate("co.aikar.locales", "xyz.mizarc.solidclaims.locales")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.javaParameters = true
}