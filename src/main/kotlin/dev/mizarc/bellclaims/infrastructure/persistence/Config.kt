package dev.mizarc.bellclaims.infrastructure.persistence
import java.io.File

import org.bukkit.plugin.Plugin

class Config(val plugin: Plugin) {
    var configFile = plugin.config

    var claimLimit = 0
    var claimBlockLimit = 0
    var initialClaimSize = 0
    var minimumPartitionSize = 0
    var distanceBetweenClaims = 0
    var visualiserDelayPeriod = 0
    var visualisationRefreshPeriod = 0
    var pluginLanguage = "EN"

    init {
        createDefaultConfig()
        loadConfig()
    }

    fun loadConfig() {
        claimLimit = configFile.getInt("claim_limit")
        claimBlockLimit = configFile.getInt("claim_block_limit")
        initialClaimSize = maxOf(3,configFile.getInt("initial_claim_size"))
        minimumPartitionSize = maxOf(3, configFile.getInt("minimum_partition_size"))
        distanceBetweenClaims = configFile.getInt("distance_between_claims")
        visualiserDelayPeriod = configFile.getInt("visualiser_delay_period")
        visualisationRefreshPeriod = configFile.getInt("visualisation_refresh_period")
        pluginLanguage = configFile.getString("plugin_language") ?: "EN"
    }

    private fun createDefaultConfig() {
        val configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false)
        }
    }
}