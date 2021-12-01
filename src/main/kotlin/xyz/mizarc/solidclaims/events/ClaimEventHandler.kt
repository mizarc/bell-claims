package xyz.mizarc.solidclaims.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.Player

/**
 * Handles the registration of defined events with their associated actions.
 */
class ClaimEventHandler(var solidClaims: SolidClaims, var claimContainer: ClaimContainer) : Listener {
    companion object {
        var handleEvents = false

        /**
         * The generic function to cancel any cancellable event.
         * TODO: Check the permissions context of this event callback.
         */
        fun cancelEvent(listener: Listener, event: Event) {
            if (handleEvents) {
                if (event is Cancellable) {
                    event.isCancelled = true
                }
            }
        }
    }

    init {
        ClaimPermission.values().forEach { p ->
            p.events.forEach { e ->
                registerEvent(e.first, e.second)
            }
        }
    }

    /**
     * An alias to the PluginManager.registerEvent() function that handles some parameters automatically.
     */
    private fun registerEvent(event: Class<out Event>, priority: EventPriority, executor: (l: Listener, e: Event) -> Unit) =
        solidClaims.server.pluginManager.registerEvent(event, this, priority, executor,
            solidClaims, true)

    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) = registerEvent(event, EventPriority.NORMAL, executor)
}
