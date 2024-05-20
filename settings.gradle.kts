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

fun add(name: String, path: String) {
    include(name)
    project(name).projectDir = file(path)
}

rootProject.name = "common-events"

add(":xplat", "core/xplat")
add(":fabric", "core/fabric")
add(":neoforge", "core/neoforge")
add(":xplat-mojmap", "core/xplat-mojmap")

add(":kotlin-xplat", "kotlin/xplat")
add(":kotlin-fabric", "kotlin/fabric")
add(":kotlin-neoforge", "kotlin/neoforge")
add(":kotlin-xplat-mojmap", "kotlin/xplat-mojmap")

add(":example-xplat", "example/xplat")
add(":example-fabric", "example/fabric")
add(":example-neoforge", "example/neoforge")

add(":example-kotlin-xplat", "example-kotlin/xplat")
add(":example-kotlin-fabric", "example-kotlin/fabric")
add(":example-kotlin-neoforge", "example-kotlin/neoforge")
