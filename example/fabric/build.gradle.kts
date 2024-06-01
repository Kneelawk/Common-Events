plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    applyXplatConnection(":example-xplat", "fabric")
    generateRuns()
}

dependencies {
    // Mod Menu
//    val mod_menu_version: String by project
//    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version") {
//        exclude(group = "net.fabricmc")
//        exclude(group = "net.fabricmc.fabric-api")
//    }
}
