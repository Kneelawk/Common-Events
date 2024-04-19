import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("dev.architectury.loom")
    id("maven-publish")
}

evaluationDependsOn(":example-kotlin-xplat")

val releaseTag = System.getenv("RELEASE_TAG")
val modVersion = if (releaseTag != null) {
    val modVersion = releaseTag.substring(1)
    println("Detected release version: $modVersion")
    modVersion
} else {
    val mod_version: String by project
    mod_version
}

version = modVersion
val maven_group: String by project
group = maven_group

base {
    val archives_base_name: String by project
    archivesName = "$archives_base_name-${project.name}"
}

loom {
    runs {
        named("client").configure {
            ideConfigGenerated(true)
            programArgs("--width", "1280", "--height", "720")
        }
        named("server").configure {
            ideConfigGenerated(true)
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.firstdark.dev/snapshots") { name = "FirstDark" }
    maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "Kotlin" }
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")
    
    // Kotlin
    val neoforge_kotlin_version: String by project
    modCompileOnly("thedarkcolour:kotlinforforge-neoforge:$neoforge_kotlin_version")
    modLocalRuntime("thedarkcolour:kotlinforforge-neoforge:$neoforge_kotlin_version")

    compileOnly(project(":example-kotlin-xplat"))

    // Common Events
    compileOnly(project(":neoforge", configuration = "namedElements"))
    // Specifically use artifact produced by a custom jar task so NeoForge will actually pick up the dependency
    runtimeOnly(project(":neoforge", configuration = "dev"))
    include(project(":neoforge"))
    
    testCompileOnly(project(":neoforge", configuration = "namedElements"))
    testRuntimeOnly(project(":neoforge", configuration = "dev"))
}

java {
    val java_version: String by project
    val javaVersion = JavaVersion.toVersion(java_version)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withSourcesJar()
}

tasks {
    processResources {
        from(project(":example-kotlin-xplat").sourceSets.main.get().resources)

        inputs.property("modVersion", modVersion)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to modVersion)
        }
    }

    withType<JavaCompile>().configureEach {
        source(project(":example-kotlin-xplat").sourceSets.main.get().allJava)
        options.encoding = "UTF-8"
        val java_version: String by project
        options.release.set(java_version.toInt())
    }

    withType<KotlinCompile>().configureEach {
        source(project(":example-kotlin-xplat").sourceSets.main.get().kotlin)
        val java_version: String by project
        kotlinOptions.jvmTarget = java_version
    }

    jar.configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    named("sourcesJar", Jar::class).configure {
        from(project(":example-kotlin-xplat").sourceSets.main.get().allSource)
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    afterEvaluate {
        named("genSources").configure {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
