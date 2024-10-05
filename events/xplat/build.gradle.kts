plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    setRefmaps("common-events-events")
    setupJavadoc()
    xplatProjectDependency(":main-bus")
    xplatProjectDependency(":", include = false)
}

kpublish {
    createPublication("intermediary")
}
