package xyz.mizarc.solidclaims.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent

/**
 * A static class object to define the behaviour of event handling for events that occur within claims where the
 * origin does not have the permission to perform such actions.
 */
class ClaimEventBehaviour {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        /**
         * The generic function to cancel any cancellable event.
         */
        fun cancelEvent(listener: Listener, event: Event) {
            if (event is Cancellable) {
                if (event is PlayerEvent) {
                    event.player.sendMessage("cancelEvent called!")
                }
                event.isCancelled = true
            }
        }
    }
}