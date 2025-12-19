package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.bukkit.entity.Player

class GetAllowedPermissions(private val config: MainConfig) {
    fun execute(player: Player, allPermissions: Set<ClaimPermission>): Set<ClaimPermission> {

        return allPermissions
            .asSequence()
            .filter { permission -> val blacklisted = isBlacklisted(permission); !blacklisted || hasPermission(player, permission) }
            .toSet()
    }

    private fun isBlacklisted(permission: ClaimPermission): Boolean {
        return config.blacklistedPermissions.any { s ->
            s.equals(permission.name, ignoreCase = true) || s.equals(permission.name, ignoreCase = true)
        }
    }

    private fun hasPermission(player: Player, permission: ClaimPermission): Boolean =
        player.hasPermission("bellclaims.permission.${permission.name.lowercase()}")
}

