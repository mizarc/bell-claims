package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.values.Flag
import org.bukkit.entity.Player

class GetAllowedFlags(private val config: MainConfig) {
    fun execute(player: Player, allFlags: Set<Flag>): Set<Flag> {

        return allFlags
            .asSequence()
            .filter { flag -> val blacklisted = isBlacklisted(flag); !blacklisted || hasPermission(player, flag) }
            .toSet()
    }

    private fun isBlacklisted(flag: Flag): Boolean {
        return config.blacklistedFlags.any { s ->
            s.equals(flag.name, ignoreCase = true) || s.equals(flag.name, ignoreCase = true)
        }
    }

    private fun hasPermission(player: Player, flag: Flag): Boolean =
        player.hasPermission("bellclaims.flag.${flag.name.lowercase()}")
}

