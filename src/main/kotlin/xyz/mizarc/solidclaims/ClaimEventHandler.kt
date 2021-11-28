package xyz.mizarc.solidclaims

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.player.*

class ClaimEventHandler : Listener {

    init {
        registerEvent(PlayerBedEnterEvent::class.java, ::cancelEvent)
        registerEvent(BlockIgniteEvent::class.java, ::cancelEvent)
    }

    private fun cancelEvent(listener: Listener, event: Event) {
        if (event is Cancellable) {
            event.isCancelled = true
            if (event is PlayerEvent) {
                event.player.sendMessage("Fuck you bitch!")
            }
        }
    }

    private fun registerEvent(event: Class<out Event>, priority: EventPriority, executor: (l: Listener, e: Event) -> Unit) =
        SolidClaims.instance.server.pluginManager.registerEvent(event, this, priority, executor, SolidClaims.instance, true)

    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) = registerEvent(event, EventPriority.NORMAL, executor);
}