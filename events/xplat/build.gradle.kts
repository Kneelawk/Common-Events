plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyMixinExpansions()
    setupJavadoc()
    xplatProjectDependency(":main-bus")
    xplatProjectDependency(":", include = false)
}

kpublish {
    createPublication("mojmap")
}
