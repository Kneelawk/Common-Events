plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    setRefmaps("common-events-test")
    setupJavadoc()
    xplatProjectDependency(":")
}

tasks.processResources {
    filesMatching("META-INF/common-events-info.properties") {
        expand("version" to project.version)
    }
}

kpublish {
    createPublication("intermediary")
}
