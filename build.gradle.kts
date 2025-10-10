import java.util.Properties

plugins {
    kotlin("jvm") version "2.2.+"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// =========================================================
// Place the property loading and fallback logic here
// =========================================================
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { inputStream ->
        localProperties.load(inputStream)
    }
}

fun getProperty(name: String): String {
    val localValue = localProperties.getProperty(name)
    if (localValue != null) {
        return localValue
    }
    return providers.gradleProperty(name).getOrElse("")
}
// =========================================================

group = "dev.mizarc"
version = "0.4.4"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
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
    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("com.github.MilkBowl:VaultAPI:1.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    shadow("org.jetbrains.kotlin:kotlin-stdlib")
    implementation ("org.slf4j:slf4j-nop:2.0.13")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("com.github.mizarc:IF:0.11.4-d")
    implementation("io.insert-koin:koin-core:4.0.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier = null
}

tasks.register<Copy>("deploy") {
    dependsOn(tasks.shadowJar)
    from(layout.buildDirectory.dir("libs"))
    println("Target deployment path: ${getProperty("plugin.server.path")}")
    into(getProperty("plugin.server.path"))
    rename { fileName -> "${rootProject.name}-${version}.jar" }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}