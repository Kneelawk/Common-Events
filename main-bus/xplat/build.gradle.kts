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

kpublish {
    createPublication("mojmap")
}
