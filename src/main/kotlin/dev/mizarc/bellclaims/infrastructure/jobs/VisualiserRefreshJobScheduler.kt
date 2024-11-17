package dev.mizarc.bellclaims.infrastructure.jobs

import org.bukkit.entity.Player

class VisualiserRefreshJobScheduler {
    private val playerJobs: MutableMap<Player, VisualiserRefreshJob> = mutableMapOf()

    fun getByPlayer(player: Player): VisualiserRefreshJob? {
        return playerJobs[player]
    }

    fun start(player: Player) {
        val visualiserRefreshJob = VisualiserRefreshJob(player)
        visualiserRefreshJob.run()
    }

    fun stop(player: Player) {
        val visualiserRefreshJob = getByPlayer(player) ?: return
        visualiserRefreshJob.cancel()
    }
}