plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    setRefmaps("common-events-main-bus")
    setupJavadoc()
    xplatProjectDependency(":")
}

kpublish {
    createPublication("intermediary")
}
