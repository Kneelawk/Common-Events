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
        val versioning_version: String by settings
        id("com.kneelawk.versioning") version versioning_version
        val kpublish_version: String by settings
        id("com.kneelawk.kpublish") version kpublish_version
        val submodule_version: String by settings
        id("com.kneelawk.submodule") version submodule_version
    }
}

fun add(enabled: Boolean, name: String, path: String) {
    if (!enabled) return
    include(name)
    project(name).projectDir = file(path)
}

rootProject.name = "common-events"

val xplat = true
val mojmap = true
val fabric = true
val neoforge = false

add(xplat, ":xplat", "core/xplat")
add(fabric, ":fabric", "core/fabric")
add(neoforge, ":neoforge", "core/neoforge")
add(mojmap, ":xplat-mojmap", "core/xplat-mojmap")

add(xplat, ":kotlin-xplat", "kotlin/xplat")
add(fabric, ":kotlin-fabric", "kotlin/fabric")
add(neoforge, ":kotlin-neoforge", "kotlin/neoforge")
add(mojmap, ":kotlin-xplat-mojmap", "kotlin/xplat-mojmap")

add(xplat, ":main-bus-xplat", "main-bus/xplat")
add(fabric, ":main-bus-fabric", "main-bus/fabric")
add(neoforge, ":main-bus-neoforge", "main-bus/neoforge")
add(mojmap, ":main-bus-xplat-mojmap", "main-bus/xplat-mojmap")

add(xplat, ":events-xplat", "events/xplat")
add(fabric, ":events-fabric", "events/fabric")
add(neoforge, ":events-neoforge", "events/neoforge")
add(mojmap, ":events-xplat-mojmap", "events/xplat-mojmap")

add(xplat, ":example-xplat", "example/xplat")
add(fabric, ":example-fabric", "example/fabric")
add(neoforge, ":example-neoforge", "example/neoforge")

add(xplat, ":example-kotlin-xplat", "example-kotlin/xplat")
add(fabric, ":example-kotlin-fabric", "example-kotlin/fabric")
add(neoforge, ":example-kotlin-neoforge", "example-kotlin/neoforge")
