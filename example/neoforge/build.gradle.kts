plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyNeoforgeDependency()
    applyXplatConnection(":example-xplat", "neoforge")
    generateRuns()
}
