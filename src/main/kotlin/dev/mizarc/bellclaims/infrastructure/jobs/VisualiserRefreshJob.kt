package dev.mizarc.bellclaims.infrastructure.jobs

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class VisualiserRefreshJob(val player: Player) : BukkitRunnable() {

    override fun run() {
        player.sendMessage("test")
    }
}