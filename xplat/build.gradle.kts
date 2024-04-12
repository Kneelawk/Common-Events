plugins {
    id("java")
    id("dev.architectury.loom")
    id("maven-publish")
}

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
    archivesName = "$archives_base_name-${project.name}-intermediary"
}

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir(project.name) })

repositories {
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")
}

java {
    val java_version: String by project
    toolchain.languageVersion.set(JavaLanguageVersion.of(java_version))

    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        val java_version: String by project
        options.release.set(java_version.toInt())
    }

    javadoc.configure {
        exclude("com/kneelawk/commonevents/impl")

        options.optionFiles(rootProject.file("javadoc-options.txt"))
    }

    jar.configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    named("sourcesJar", Jar::class).configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${rootProject.name}-${project.name}-intermediary"
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
