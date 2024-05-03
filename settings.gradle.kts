pluginManagement {
    repositories {
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }
        maven("https://kneelawk.com/maven/") {
            name = "Kneelawk"
        }
        gradlePluginPortal()
    }
    plugins {
        val kotlin_version: String by settings
        kotlin("jvm") version kotlin_version
        val architectury_loom_version: String by settings
        id("dev.architectury.loom") version architectury_loom_version
    }
}

rootProject.name = "common-events"

include(":xplat")
include(":fabric")
include(":neoforge")

include(":example-xplat")
project(":example-xplat").projectDir = file("example/xplat")
include(":example-fabric")
project(":example-fabric").projectDir = file("example/fabric")
include(":example-neoforge")
project(":example-neoforge").projectDir = file("example/neoforge")

include(":example-kotlin-xplat")
project(":example-kotlin-xplat").projectDir = file("example-kotlin/xplat")
include(":example-kotlin-fabric")
project(":example-kotlin-fabric").projectDir = file("example-kotlin/fabric")
include(":example-kotlin-neoforge")
project(":example-kotlin-neoforge").projectDir = file("example-kotlin/neoforge")
