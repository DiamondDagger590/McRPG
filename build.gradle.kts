import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`
    `java-test-fixtures`
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
version = "2.0.0.4-SNAPSHOT"
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
    maven("https://maven.enginehub.org/repo/") //WorldGuard
    maven("https://nexus.wesjd.net/repository/thirdparty/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://repo.lunarclient.dev") //Lunar client
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //Papi

    //Spigot
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/central/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")

}

dependencies {

    // Force annotations to be 12 because @NotNull being inline was annoying me from latest
    val intellijAnnotationVersion = "12.0"
    compileOnlyApi("com.intellij:annotations:$intellijAnnotationVersion")

    val mccoreVersion = "1.0.0.17-SNAPSHOT"
    implementation("com.diamonddagger590:McCore:$mccoreVersion")
    testImplementation(testFixtures("com.diamonddagger590:McCore:$mccoreVersion"))
    testFixturesImplementation(testFixtures("com.diamonddagger590:McCore:$mccoreVersion"))

    val paperVersion = "1.21.8-R0.1-20250822.103624-38"
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    testImplementation("io.papermc.paper:paper-api:$paperVersion")
    testFixturesImplementation("io.papermc.paper:paper-api:$paperVersion")

    val bstatsVersion = "2.2.1"
    implementation("org.bstats:bstats-bukkit:$bstatsVersion")

    val glowingEntitiesVersion = "1.4.5"
    implementation("fr.skytasul:glowingentities:$glowingEntitiesVersion")

    val customBlockDataVersion = "2.2.2"
    implementation("com.jeff-media:custom-block-data:$customBlockDataVersion")

    val worldGuardVersion = "7.0.7"
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:$worldGuardVersion")

    val geyserVersion = "2.4.2-SNAPSHOT"
    compileOnly("org.geysermc.geyser:api:$geyserVersion")

    val apolloVersion = "1.1.8"
    compileOnly("com.lunarclient:apollo-api:$apolloVersion")

    val landsVersion = "7.10.13"
    compileOnly("com.github.angeschossen:LandsAPI:$landsVersion")

    // Test deps
    val mockBukkitVersion = "4.72.8"
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:$mockBukkitVersion")
    testFixturesImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:$mockBukkitVersion")

    // Gotta be explicit (MockBukkit is throwing a fit)
    val junitVersion = "5.11.0";
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testFixturesImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    val mockitoVersion = "3.12.2";
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testFixturesImplementation("org.mockito:mockito-inline:$mockitoVersion")

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
        relocate("fr.skytasul.glowingentites", "us.eunoians.mcrpg.glowingentites")
    }
}

tasks.register("verifiedShadowJar") {
    group = "build"
    description = "Runs tests, then builds the shaded jar."
    dependsOn("clean", "test", "shadowJar")
}

tasks.register("fastShadowJar") {
    group = "build"
    description = "Builds the shaded jar without tests."
    dependsOn("clean", "shadowJar")
}