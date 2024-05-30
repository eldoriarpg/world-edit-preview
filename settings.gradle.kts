rootProject.name = "world-edit-preview"
include("api")
include("plugin")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("worldedit", "com.sk89q.worldedit:worldedit-bukkit:7.2.18")
            version("fawe", "2.8.4")
            library("fawe-core", "com.fastasyncworldedit", "FastAsyncWorldEdit-Core").versionRef("fawe")
            library("fawe-bukkit", "com.fastasyncworldedit", "FastAsyncWorldEdit-Bukkit").versionRef("fawe")
            bundle("fawe", listOf("fawe-core", "fawe-bukkit"))

                        version("minecraft-latest", "1.20.1-R0.1-SNAPSHOT")
            library("paper-latest", "io.papermc.paper", "paper-api").versionRef("minecraft-latest")
            library("spigot-latest", "io.papermc.paper", "paper-api").versionRef("minecraft-latest")
            bundle("minecraft-latest", listOf("paper-latest", "spigot-latest"))


            // plugins
            plugin("publishdata", "de.chojo.publishdata").version("1.3.0")
            plugin("spotless", "com.diffplug.spotless").version("6.24.0")

        }
    }
}
include("renderer")
