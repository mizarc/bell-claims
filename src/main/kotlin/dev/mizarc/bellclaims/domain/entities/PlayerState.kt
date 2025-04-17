package dev.mizarc.bellclaims.domain.entities

import dev.mizarc.bellclaims.domain.values.Position3D
import org.bukkit.scheduler.BukkitRunnable
import java.time.Instant
import java.util.UUID

/**
 * Holds temporary player state data mainly pertaining to claim editing.
 *
 * @property playerId The id of the player.
 */
class PlayerState(val playerId: UUID) {
    var claimOverride = false
    var claimToolMode = 0
    var isHoldingClaimTool = false
    var isVisualisingClaims = false
    var isInClaimMenu: UUID? = null
    var scheduledVisualiserHide: BukkitRunnable? = null
    var lastVisualisationTime: Instant? = null

    // Map claim id to block positions
    var visualisedClaims: MutableMap<UUID, Set<Position3D>> = mutableMapOf()

    // Map claim id to partition id to block positions
    var visualisedPartitions: MutableMap<UUID, MutableMap<UUID, Set<Position3D>>> = mutableMapOf()
}