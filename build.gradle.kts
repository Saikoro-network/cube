/**
 * Copyright (C) 2022 TTtie
 *
 * This file is part of Cube.
 *
 * Cube is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Cube.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.streams.toList

plugins {
    kotlin("jvm") version ("1.7.10")
    id("com.github.johnrengelman.shadow") version ("5.2.0")
}

group = "network.saikoro"
version = "1.1.0"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    //components.all(KotlinAlignment::class.java)
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")

    compileOnly(fileTree("libs/") {
        include("*jar")
    })

    shadow(kotlin("stdlib-jdk8"))
    shadow(kotlin("reflect"))
    shadow("com.zaxxer:HikariCP:5.0.1")
    shadow("org.ktorm:ktorm-core:3.5.0")
    /*shadow*/ compileOnly("com.maxmind.geoip2:geoip2:3.0.1")

    shadow("net.kyori:adventure-api:4.11.0")
    shadow("net.kyori:adventure-platform-bungeecord:4.1.2")
    // shadow("net.kyori:adventure-platform-bukkit:4.0.0")

    shadow("club.minnced:discord-webhooks:0.8.2")
    shadow("org.apache.commons:commons-text:1.9")
}

val makeShadow = tasks.register<ShadowJar>("makeShadow") {
    archiveClassifier.set("shade")
    from(sourceSets.main.orNull?.output)
    configurations = mutableListOf(project.configurations.shadow.get())

    dependsOn("relocateShadows")

    doFirst {
        relocators = relocators.parallelStream().filter {
            if (it !is SimpleRelocator) return@filter true
            it.pattern != "kotlin" && !it.pattern.startsWith("kotlin.")
        }.toList()
    }
}

tasks {
    "11".also {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> { kotlinOptions.jvmTarget = it }
    }
    named("build") {
        dependsOn("makeShadow")
    }

    // To implementors: you must relocate kotlin references to network.saikoro.core.lib.kotlin etc.
    register<ConfigureShadowRelocation>("relocateShadows") {
        target = makeShadow.get()
        prefix = "network.saikoro.core.lib"
    }

    processResources {
        doFirst {
            expand("version" to version)
        }
    }
}
