import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "6.3.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
version = "1.5.1.0"
group = "us.eunoians"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()

    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //Papi
    maven("https://maven.sk89q.com/repo/") //WorldGuard
    maven("https://nexus.wesjd.net/repository/thirdparty/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")

    //Spigot
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

}

dependencies {

    val intellijAnnotationVersion = "12.0"
    implementation("com.intellij:annotations:$intellijAnnotationVersion")

    val spigotVersion = "1.18.1-R0.1-SNAPSHOT"
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")

    // TODO Look into new spigot yaml comment api
    val enumToYamlVersion = "1.0"
    implementation("com.github.DiamondDagger590:EnumToYaml:$enumToYamlVersion")

    val nbtAPIVersion = "2.9.2-SNAPSHOT"
    implementation("de.tr7zw:item-nbt-api:$nbtAPIVersion")

    val bstatsVersion = "2.2.1"
    implementation("org.bstats:bstats-bukkit:$bstatsVersion")

    val placeholderAPIVersion = "2.9.2"
    compileOnly("me.clip:placeholderapi:$placeholderAPIVersion")

    val worldGuardVersion = "7.0.0-SNAPSHOT"
    compileOnly("com.sk89q.worldguard:worldguard-core:$worldGuardVersion")
    compileOnly("com.sk89q.worldguard:worldguard-legacy:$worldGuardVersion")

    //Jar deps
    compileOnly(files("libs/Sickle.jar"))
    compileOnly(files("libs/SpartanAPI.jar"))
    compileOnly(files("libs/mcMMO.jar"))
    compileOnly(files("libs/NoCheatPlus.jar")) //3.16.0-RC-sMD5NET-b1134
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources>{
    filesMatching("**/*.yml"){
        expand(project.properties)
    }
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
    named<ShadowJar>("shadowJar") {

        //My gheto solution to get the commit hash on the shadow'd jar

        // Open git
        val git = org.ajoberstar.grgit.Grgit.open(file("."))
        // Use abbreviated id from git head
        archiveAppendix.set(git.head().abbreviatedId)

        // check if classifier is present before adding an unnecessary '-'.
        val classifier = archiveClassifier.get()

        // Set our desired formatting for the file name
        archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-${archiveAppendix.get()}${if (classifier.isEmpty()) "" else "-$classifier"}.${archiveExtension.get()}")

        mergeServiceFiles()
        relocate("de.tr7zw.changeme.nbtapi", "us.eunoians.mcrpg.nbtapi")
        relocate("org.bstats", "us.eunoians.mcrpg")
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
    publish {
        dependsOn(compileJava)
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}