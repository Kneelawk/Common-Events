plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyKotlin("neoforge")
    applyNeoforgeDependency()
    applyXplatConnection(":kotlin-xplat", "neoforge")
    createDevExport()
}

kpublish {
    createPublication()
}
