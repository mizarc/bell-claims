package dev.mizarc.bellclaims.domain.players

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.scheduler.BukkitRunnable
import java.time.Instant

/**
 * Holds temporary player state data mainly pertaining to claim editing.
 *
 * @property player The reference to the player.
 */
class PlayerState(val player: OfflinePlayer) {
    var claimOverride = false
    var claimToolMode = 0
    var isHoldingClaimTool = false
    var isVisualisingClaims = false
    var visualisedBlockPositions: MutableMap<Claim, Set<Position3D>> = mutableMapOf()
    var isInClaimMenu: Claim? = null
    var scheduledVisualiserHide: BukkitRunnable? = null
    var lastVisualisationTime: Instant? = null

    /**
     * Gets the online version of the player instance.
     *
     * @return The online player instance.
     */
    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }
}