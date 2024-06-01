plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    applyXplatConnection(":events-xplat", "fabric")
    setupJavadoc()
}

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir(project.name) })

kpublish {
    createPublication()
}
