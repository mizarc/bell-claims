package dev.mizarc.bellclaims.infrastructure.jobs

import dev.mizarc.bellclaims.api.VisualiserRefreshJobScheduler
import org.bukkit.entity.Player

class VisualiserRefreshJobSchedulerImpl: VisualiserRefreshJobScheduler {
    private val playerJobs: MutableMap<Player, VisualiserRefreshJob> = mutableMapOf()

    override fun getByPlayer(player: Player): VisualiserRefreshJob? {
        return playerJobs[player]
    }

    override fun start(player: Player) {
        val visualiserRefreshJob = VisualiserRefreshJob(player)
        visualiserRefreshJob.run()
    }

    override fun stop(player: Player) {
        val visualiserRefreshJob = getByPlayer(player) ?: return
        visualiserRefreshJob.cancel()
    }
}