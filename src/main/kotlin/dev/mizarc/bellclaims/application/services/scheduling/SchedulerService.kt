package dev.mizarc.bellclaims.application.services.scheduling

/**
 * Schedules an event to run after an X amount of time
 */
interface SchedulerService {
    fun executeOnMain(task: () -> Unit)
    fun schedule(delayTicks: Long, task: () -> Unit): Task
}