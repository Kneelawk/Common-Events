plugins {
    kotlin("jvm") apply false
    id("fabric-loom") apply false
    id("com.kneelawk.submodule") apply false
    id("agency.highlysuspect.minivan") apply false
    id("net.neoforged.moddev") apply false
}

tasks.create("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
