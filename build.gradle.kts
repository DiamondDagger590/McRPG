import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.7"
    kotlin("jvm")
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.ajoberstar:gradle-git:1.2.0")
    }
}

apply {
    plugin("java")
}

//RECODE.RELEASE.PATCH.DEVELOPMENT
version = "2.0.0.0-SNAPSHOT"
group = "us.eunoians"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //Papi
    maven("https://maven.enginehub.org/repo/") //WorldGuard
    maven("https://nexus.wesjd.net/repository/thirdparty/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.opencollab.dev/main/")

    //Spigot
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/central/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")


}

dependencies {

    val mccoreVersion = "1.0.0.10-SNAPSHOT"
    implementation("com.diamonddagger590:McCore:$mccoreVersion")

    val bstatsVersion = "2.2.1"
    implementation("org.bstats:bstats-bukkit:$bstatsVersion")

    val placeholderAPIVersion = "2.9.2"
    compileOnly("me.clip:placeholderapi:$placeholderAPIVersion")

    val worldGuardVersion = "7.0.7"
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:$worldGuardVersion")

    val customBlockDataVersion = "2.2.2"
    implementation("com.jeff-media:custom-block-data:$customBlockDataVersion")

    val geyserVersion = "2.4.2-SNAPSHOT"
    compileOnly("org.geysermc.geyser:api:$geyserVersion")

    // Test deps
    val mockBukkitVersion = "3.80.0"
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:$mockBukkitVersion")
    val junitVersion = "5.8.2";
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    val mockitoVersion = "3.12.2";
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")

    //Jar deps
    compileOnly(files("libs/Sickle.jar"))
    compileOnly(files("libs/SpartanAPI.jar"))
    compileOnly(files("libs/mcMMO.jar"))
    compileOnly(files("libs/NoCheatPlus.jar")) //3.16.0-RC-sMD5NET-b1134
    //implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filesMatching("**/*.yml") {
        expand(project.properties)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {

    manifest.attributes["Manifest-Version"] = "1.0"
    manifest.attributes["Main-Class"] = "us.eunoians.mcrpg.McRPG"
    manifest.attributes["Class-Path"] = "McRPG/libs/h2.jar"

    // Open git
    val git = org.ajoberstar.grgit.Grgit.open(file("."))
    // Use abbreviated id from git head
    archiveAppendix.set(git.head().abbreviatedId)

    // check if classifier is present before adding an unnecessary '-'.
    val classifier = archiveClassifier.get()

    // Set our desired formatting for the file name
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-${archiveAppendix.get()}${if (classifier.isEmpty()) "" else "-$classifier"}.${archiveExtension.get()}")
}

tasks {
    shadowJar {
        // Open git
        val git = org.ajoberstar.grgit.Grgit.open(file("."))
        // Use abbreviated id from git head
        archiveAppendix.set(git.head().abbreviatedId)

        // check if classifier is present before adding an unnecessary '-'.
        val classifier = archiveClassifier.get()

        // Set our desired formatting for the file name
        archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-${archiveAppendix.get()}${if (classifier.isEmpty()) "" else "-$classifier"}.${archiveExtension.get()}")

        mergeServiceFiles()
        relocate("org.bstats", "us.eunoians.mcrpg")
        relocate("com.diamonddagger590.mccore", "us.eunoians.mcrpg.mccore")
        relocate("com.jeff_media.customblockdata", "us.eunoians.mcrpg.customblockdata")
    }
}

tasks {
    build {
        dependsOn(compileJava)
        dependsOn(shadowJar)
    }
    jar {
        dependsOn(shadowJar)
    }
}