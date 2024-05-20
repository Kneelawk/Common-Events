plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyKotlin("fabric")
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    applyXplatConnection(":kotlin-xplat", "fabric")
}

kpublish {
    createPublication()
}
