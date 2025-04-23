package dev.mizarc.bellclaims.infrastructure.services.scheduling

import dev.mizarc.bellclaims.application.services.scheduling.Task
import org.bukkit.scheduler.BukkitRunnable

class TaskBukkit(private val runnable: BukkitRunnable) : Task {
    override fun cancel() {
        runnable.cancel()
    }
}