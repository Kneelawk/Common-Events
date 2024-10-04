plugins {
    id("com.kneelawk.submodule")
    id("com.kneelawk.versioning")
    id("com.kneelawk.kpublish")
}

submodule {
    applyXplatConnection(":example-xplat")
    generateRuns()
}

dependencies {
    // Mod Menu
    val mod_menu_version: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version") {
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
    }
}

loom {
    runs {
        named("client") {
            property("com.kneelawk.common_events.export_generated_classes", "true")
        }
    }
}
