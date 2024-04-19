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
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")
    modLocalRuntime("net.fabricmc:fabric-loader:$fabric_loader_version")

    // Fabric Api
    val fapi_version: String by project
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fapi_version")
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fapi_version")

    // Kotlin
    val fabric_kotlin_version: String by project
    modCompileOnly("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")
    modLocalRuntime("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    compileOnly(project(":example-kotlin-xplat"))

    // Common Events
    implementation(project(":fabric", configuration = "namedElements"))
    include(project(":fabric"))

    // Mod Menu
    val mod_menu_version: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version") {
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
    }
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

        filesMatching("fabric.mod.json") {
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
