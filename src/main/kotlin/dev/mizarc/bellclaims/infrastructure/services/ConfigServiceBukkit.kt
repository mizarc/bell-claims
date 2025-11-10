package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.ConfigService
import dev.mizarc.bellclaims.config.MainConfig
import org.bukkit.configuration.file.FileConfiguration

class ConfigServiceBukkit(private val config: FileConfiguration): ConfigService {
    override fun loadConfig(): MainConfig {
        val flags = config.getStringList("blacklisted_flags")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val permissions = config.getStringList("blacklisted_permissions")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        return MainConfig(
            claimLimit = config.getInt("claim_limit"),
            claimBlockLimit = config.getInt("claim_block_limit"),
            initialClaimSize = config.getInt("initial_claim_size"),
            minimumPartitionSize = config.getInt("minimum_partition_size"),
            distanceBetweenClaims = config.getInt("distance_between_claims"),
            visualiserHideDelayPeriod = config.getDouble("visualiser_hide_delay_period"),
            visualiserRefreshPeriod = config.getDouble("visualiser_refresh_period"),
            autoRefreshVisualisation = config.getBoolean("auto_refresh_visualisation", true),
            pluginLanguage = config.getString("plugin_language") ?: "",
            showClaimEnterPopup = config.getBoolean("show_claim_enter_popup", true),
            customClaimToolModelId = config.getInt("custom_claim_tool_model_id"),
            customMoveToolModelId = config.getInt("custom_move_tool_model_id"),
            blacklistedFlags = flags,
            blacklistedPermissions = permissions
        )
    }
}