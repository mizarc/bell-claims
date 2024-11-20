package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.infrastructure.jobs.VisualiserRefreshJob
import org.bukkit.entity.Player

/**
 * A scheduler that handles the automatic visualiser refreshing of players.
 */
interface VisualiserRefreshJobScheduler {

    /**
     * Get the active visualiser refresh job by player.
     *
     * @param player The target player.
     * @return The visualiser refresh job linked to the player.
     */
    fun getByPlayer(player: Player): VisualiserRefreshJob?

    /**
     * Starts a visualiser refresh job on a target player.
     *
     * @param player The target player.
     */
    fun start(player: Player)

    /**
     * Stops a visualiser refresh job on a target player.
     *
     * @param player The target player.
     */
    fun stop(player: Player)
}