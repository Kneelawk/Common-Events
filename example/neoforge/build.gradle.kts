plugins {
    id("java")
    id("dev.architectury.loom")
    id("maven-publish")
}

evaluationDependsOn(":example-xplat")
evaluationDependsOn(":neoforge")

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
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")

    compileOnly(project(":example-xplat"))

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
        from(project(":example-xplat").sourceSets.main.get().resources)

        inputs.property("modVersion", modVersion)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to modVersion)
        }
    }

    withType<JavaCompile>().configureEach {
        source(project(":example-xplat").sourceSets.main.get().allSource)
        options.encoding = "UTF-8"
        val java_version: String by project
        options.release.set(java_version.toInt())
    }

    jar.configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    named("sourcesJar", Jar::class).configure {
        from(project(":example-xplat").sourceSets.main.get().allSource)
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }
}
