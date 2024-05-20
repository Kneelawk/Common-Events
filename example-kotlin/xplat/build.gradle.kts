plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyKotlin("xplat")
    setRefmaps("common-events-example-kotlin")
    applyFabricLoaderDependency()
    forceRemap()
    xplatProjectDependency(":")
}
