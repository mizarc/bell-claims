package xyz.mizarc.solidclaims.events

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimContainer

/**
 * Handles the registration of defined events with their associated actions.
 * @property solidClaims A reference to the plugin instance
 * @property claimContainer A reference to the ClaimContainer instance
 */
class ClaimEventHandler(var solidClaims: SolidClaims, var claimContainer: ClaimContainer) : Listener {
    companion object {
        var handleEvents = false
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
        val claim = claimContainer.getClaimPartitionAtLocation(location)?.claim ?: return
        val player = solidClaims.database.getPlayerClaimPermissions(event.player.uniqueId, claim.id)

        val claimPerms = player?.claimPermissions ?: claim.defaultPermissions
        val eventPerms = ClaimPermission.getPermissionsForEvent(event::class.java)

        var executor: ((l: Listener, e: Event) -> Unit)? = null

        fun checkPermissionParents(p: ClaimPermission): Boolean {
            var pRef: ClaimPermission? = p
            while (pRef?.parent != null) {
                if (claimPerms.contains(pRef.parent)) {
                    return true
                }
                pRef = pRef.parent
            }
            return false
        }

        for (e in eventPerms) {
            if (claimPerms.contains(e) || checkPermissionParents(e)) {
                for (ee in e.events) {
                    if (ee.first == event::class.java) {
                        executor = ee.second
                        break
                    }
                }
            }
        }

        executor?.invoke(listener, event)
    }

    /**
     * An alias to the PluginManager.registerEvent() function that handles some parameters automatically.
     */
    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) =
        solidClaims.server.pluginManager.registerEvent(event, this, EventPriority.NORMAL, executor,
            solidClaims, true)
}
