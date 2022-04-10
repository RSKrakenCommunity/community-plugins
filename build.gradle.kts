plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("kraken.community.plugin") apply false
}

group = "com.rshub"
version = "1.0-SNAPSHOT"

subprojects {

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }

    apply {
        plugin("kotlin")
        plugin("kraken.community.plugin")
    }

    val implementation by configurations
    dependencies {
        implementation(kotlin("stdlib"))
        implementation("com.github.RSKraken:KrakenAPI:master-SNAPSHOT")
        implementation("com.rshub.api:KrakenCommunityAPI:1.0-SNAPSHOT")
    }

    tasks.withType<Jar> {
        from(configurations.named("runtimeClasspath").get().map { if(it.isDirectory) it else zipTree(it) })
    }
}

