package dev.mizarc.bellclaims.infrastructure.services.scheduling

import dev.mizarc.bellclaims.application.services.scheduling.SchedulerService
import dev.mizarc.bellclaims.application.services.scheduling.Task
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

class SchedulerServiceBukkit(private val plugin: Plugin): SchedulerService {
    override fun schedule(delayTicks: Long, task: () -> Unit): Task {
        val runnable = object : BukkitRunnable() {
            override fun run() {
                task()
            }
        }
        runnable.runTaskLater(plugin, delayTicks)
        return TaskBukkit(runnable)
    }
}