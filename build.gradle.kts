plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.mizarc"
version = "0.3.5"

repositories {
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
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
    maven {
        url = uri("https://repo.opencollab.dev/main/")
    }
        mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    testImplementation("com.github.MilkBowl:VaultAPI:1.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    shadow("org.jetbrains.kotlin:kotlin-stdlib")
    implementation ("org.slf4j:slf4j-nop:2.0.13")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.17")
    implementation("net.wesjd:anvilgui:1.10.3-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.geysermc.geyser:api:2.4.2-SNAPSHOT")
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