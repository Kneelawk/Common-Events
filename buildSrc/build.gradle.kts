/*
 * Copyright (c) 2024 Cyan Kneelawk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.9.20"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.architectury.dev/") { name = "Architectury" }
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://kneelawk.com/maven") { name = "Kneelawk" }
}

dependencies {
    val architectury_loom_version: String by project
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:$architectury_loom_version")
    
    val kotlin_version: String by project
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlin_version")
}

gradlePlugin {
    plugins {
        create("versioningPlugin") {
            id = "com.kneelawk.versioning"
            implementationClass = "com.kneelawk.versioning.VersioningPlugin"
        }
        create("submodulePlugin") {
            id = "com.kneelawk.submodule"
            implementationClass = "com.kneelawk.submodule.SubmodulePlugin"
        }
        create("kpublishPlugin") {
            id = "com.kneelawk.kpublish"
            implementationClass = "com.kneelawk.kpublish.KPublishPlugin"
        }
    }
}
