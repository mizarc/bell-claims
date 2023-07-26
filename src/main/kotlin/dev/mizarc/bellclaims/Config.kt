package dev.mizarc.bellclaims

import org.bukkit.plugin.Plugin

class Config(val plugin: Plugin) {
    private val configFile = plugin.config

    var claimLimit = 0
    var claimBlockLimit = 0
    var minimumClaimSize = 0
    var distanceBetweenClaims = 0

    init {
        createDefaultConfig()
        loadConfig()
    }

    fun loadConfig() {
        claimLimit = configFile.getInt("claim_limit")
        claimBlockLimit = configFile.getInt("claim_block_limit")
        minimumClaimSize = configFile.getInt("minimum_claim_size")
        distanceBetweenClaims = configFile.getInt("distance_between_claims")
    }

    private fun createDefaultConfig() {
        plugin.config.addDefault("claim_limit", 3)
        plugin.config.addDefault("claim_block_limit", 5000)
        plugin.config.addDefault("minimum_claim_size", 5)
        plugin.config.addDefault("distance_between_claims", 3)
        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
    }
}