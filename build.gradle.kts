plugins {
    kotlin("jvm") apply false
    id("dev.architectury.loom") apply false
}

tasks.create("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    tasks {
        // make builds reproducible
        withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }
}
