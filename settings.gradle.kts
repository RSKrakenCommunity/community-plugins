pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if(this.requested.id.id == "kraken.community.plugin") {
                useModule("com.github.RSKrakenCommunity:Kraken-Gradle-Plugin:master-SNAPSHOT")
            }
        }
    }
}

rootProject.name = "Kraken-Community-Plugins"
include(":lodestone-unlocker")
include(":quality-of-life")
include(":spring-event")

