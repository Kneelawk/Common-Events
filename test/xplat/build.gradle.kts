plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyMixinExpansions()
    setupJavadoc()
    xplatProjectDependency(":")
}

tasks.processResources {
    filesMatching("META-INF/common-events-info.properties") {
        expand("version" to project.version)
    }
}

kpublish {
    createPublication("mojmap")
}
