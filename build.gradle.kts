plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("kraken.community.plugin") apply false
}

group = "com.rshub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("kraken.community.plugin")
    }
    val implementation by configurations
    dependencies {
        implementation(kotlin("stdlib"))
    }
}

