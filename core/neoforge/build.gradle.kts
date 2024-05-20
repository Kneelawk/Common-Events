plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyNeoforgeDependency()
    applyXplatConnection(":xplat", "neoforge")
    setupJavadoc()
    createDevExport()
}

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir(project.name) })

kpublish {
    createPublication()
}
