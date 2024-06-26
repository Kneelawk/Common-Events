plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    setRefmaps("common-events-events")
    applyFabricLoaderDependency()
    forceRemap()
    setupJavadoc()
    xplatProjectDependency(":main-bus")
}

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir(project.name) })

kpublish {
    createPublication("intermediary")
}
