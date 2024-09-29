package dev.mizarc.bellclaims.infrastructure.persistence
import java.io.File

import org.bukkit.plugin.Plugin

class Config(val plugin: Plugin) {
    var configFile = plugin.config

    var claimLimit = 0
    var claimBlockLimit = 0
    var minimumPartitionSize = 0
    var distanceBetweenClaims = 0
    var pluginLanguage = "EN"

    init {
        createDefaultConfig()
        loadConfig()
    }

    fun loadConfig() {
        claimLimit = configFile.getInt("claim_limit")
        claimBlockLimit = configFile.getInt("claim_block_limit")
        minimumPartitionSize = configFile.getInt("minimum_partition_size")
        distanceBetweenClaims = configFile.getInt("distance_between_claims")
        pluginLanguage = configFile.getString("plugin_language") ?: "EN"
    }

    private fun createDefaultConfig() {

        val configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false)
        }

        //disabled for comments in config.yml
        /*
        plugin.config.addDefault("claim_limit", 3)
        plugin.config.addDefault("claim_block_limit", 5000)
        plugin.config.addDefault("minimum_claim_size", 5)
        plugin.config.addDefault("distance_between_claims", 3)
        plugin.config.addDefault("plugin_language", "EN")

        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
        */

    }
}