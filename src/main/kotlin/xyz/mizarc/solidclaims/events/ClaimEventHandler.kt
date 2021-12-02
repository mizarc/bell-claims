package xyz.mizarc.solidclaims.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPlayer

/**
 * Handles the registration of defined events with their associated actions.
 * @property solidClaims A reference to the plugin instance
 * @property claimContainer A reference to the ClaimContainer instance
 */
class ClaimEventHandler(var solidClaims: SolidClaims, var claimContainer: ClaimContainer) : Listener {
    @Suppress("UNUSED_PARAMETER")
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
                registerEvent(e.first, ::handleClaimEvent)
            }
        }
    }

    /**
     * A wrapper function to abstract the business logic of determining if an event occurs within a claim, if the
     * player that the event originated from has permissions within that claim, and if not, which permission event
     * executor has the highest priority, then invoke that executor.
     */
    private fun handleClaimEvent(listener: Listener, event: Event) {
        if (!handleEvents) return // TODO: Remove debug
        if (event !is PlayerEvent) return // TODO: Check for non-player events to handle
        val location = event.player.location
        val claim = claimContainer.getClaimAtLocation(location) ?: return
        val player = ClaimPlayer(event.player.uniqueId) // TODO: Get an actual player instead of constructing one

        // TODO: If player is not in claim list, use default permissions
        if (!(claim.claimPlayers.contains(player))) {
            var priority = Int.MAX_VALUE // Higher number == lower priority
            var executor: ((l: Listener, e: Event) -> Unit)? = null
            player.claimPermissions.forEach { p ->
                if (priority < p.priority) return@forEach
                run events@ {
                    p.events.forEach { e ->
                        if (e.first == event::class.java) {
                            priority = p.priority
                            executor = e.second
                            return@events
                        }
                    }
                }
            }
            executor?.invoke(listener, event)
        }
    }

    /**
     * An alias to the PluginManager.registerEvent() function that handles some parameters automatically.
     */
    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) =
        solidClaims.server.pluginManager.registerEvent(event, this, EventPriority.NORMAL, executor,
            solidClaims, true)
}
