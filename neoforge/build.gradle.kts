plugins {
    id("java")
    id("dev.architectury.loom")
    id("maven-publish")
}

evaluationDependsOn(":xplat")

val releaseTag = System.getenv("RELEASE_TAG")
val modVersion = if (releaseTag != null) {
    val modVersion = releaseTag.substring(1)
    println("Detected release version: $modVersion")
    modVersion
} else {
    val mod_version: String by project
    mod_version
}

version = modVersion
val maven_group: String by project
group = maven_group

base {
    val archives_base_name: String by project
    archivesName = "$archives_base_name-${project.name}"
}

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })
java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir(project.name) })

configurations {
    create("dev") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.firstdark.dev/snapshots") { name = "FirstDark" }
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")

    compileOnly(project(":xplat"))
}

java {
    val java_version: String by project
    val javaVersion = JavaVersion.toVersion(java_version)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withSourcesJar()
    withJavadocJar()
}

tasks {
    processResources {
        from(project(":xplat").sourceSets.main.get().resources)

        inputs.property("modVersion", modVersion)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to modVersion)
        }
    }

    withType<JavaCompile>().configureEach {
        source(project(":xplat").sourceSets.main.get().allSource)
        options.encoding = "UTF-8"
        val java_version: String by project
        options.release.set(java_version.toInt())
    }

    withType<Javadoc>().configureEach {
        source(project(":xplat").sourceSets.main.get().java)

        exclude("com/kneelawk/commonevents/impl")

        options.optionFiles(rootProject.file("javadoc-options.txt"))
    }

    jar.configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    named("sourcesJar", Jar::class).configure {
        from(project(":xplat").sourceSets.main.get().allSource)
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    // Brute force prevent gradle from just putting project build dirs on classpath
    create("jarExt", Jar::class) {
        from(compileJava)
        from(processResources)
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
        archiveClassifier = "jarExt"
        destinationDirectory.set(project.layout.buildDirectory.dir("devlibs"))
    }

    assemble.configure {
        dependsOn("jarExt")
    }
}

artifacts {
    add("dev", tasks.getByName("jarExt"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${rootProject.name}-${project.name}"
            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("PUBLISH_REPO") != null) {
            maven {
                name = "publishRepo"
                url = uri(rootProject.file(System.getenv("PUBLISH_REPO")))
            }
        }
    }
}
