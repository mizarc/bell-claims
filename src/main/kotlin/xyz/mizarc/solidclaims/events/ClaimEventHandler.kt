package xyz.mizarc.solidclaims.events

import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.PlayerAccess

/**
 * Handles the registration of defined events with their associated actions.
 * @property plugin A reference to the plugin instance
 * @property claimContainer A reference to the ClaimContainer instance
 */
class ClaimEventHandler(var plugin: SolidClaims, var claimContainer: ClaimContainer) : Listener {
    init {
        for (perm in ClaimPermission.values()) {
            for (e in perm.events) {
                registerEvent(e.eventClass, ::handleClaimEvent)
            }
        }
    }

    /**
     * A wrapper function to abstract the business logic of determining if an event occurs within a claim, if the
     * player that the event originated from has permissions within that claim, and if not, which permission event
     * executor has the highest priority, then invoke that executor.
     */
    private fun handleClaimEvent(listener: Listener, event: Event) {
        val eventPerms = ClaimPermission.getPermissionsForEvent(event::class.java) // Get all ClaimEvents that deal with this event

        // Get the top PermissionExecutor that deals with this event.
        // NOTE: This assumes that any PermissionExecutor that deals with this event will always return the same values
        // for location and player as any other for this event would
        val tempExecutor = ClaimPermission.getPermissionExecutorForEvent(event::class.java) ?: return

        val player: Player? = tempExecutor.source.invoke(event) // The player that caused this event, if any
        val location: Location = tempExecutor.location.invoke(event) ?: return // If no location was found, do nothing

        // Determine if this event happened inside of a claim's boundaries
        val claim = plugin.claimContainer.getClaimPartitionAtLocation(location)?.claim ?: return

        // If player is owner, do nothing.
        if (player != null) {
            if (player.uniqueId == claim.owner.uniqueId) {
                return
            }
        }

        var claimTrustee: PlayerAccess? = null // The relevant claim's trustee, if the player is trusted
        for (p in claim.playerAccesses) {
            if (p.id == player?.uniqueId) {
                claimTrustee = p
                break
            }
        }

        // Get the claim permissions to use, whether it's the trustee's individual permissions, or the claim's default permissions
        val claimPerms = claimTrustee?.claimPermissions ?: claim.defaultPermissions

        var executor: ((l: Listener, e: Event) -> Unit)? = null // The function that handles the result of this event

        // Determine if the claim permissions contains any of the parent permissions to this one
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

        // Determine the highest priority permission for the event and sets the executor to the one found, if any
        for (e in eventPerms) {
            if (!checkPermissionParents(e)) { // First check if claimPerms does not contain the parent of this permission
                if (!claimPerms.contains(e)) { // If not, check if it does not contain this permission
                    for (ee in e.events) { // If so, determine the executor to use
                        if (ee.eventClass == event::class.java) {
                            executor = ee.executor
                            break
                        }
                    }
                }
            }
        }

        executor?.invoke(listener, event)
        // If nothing was executed then the player has permissions to enact this event, so do not send a warning.
        if (executor != null) player?.sendMessage("${ChatColor.RED}You are not allowed to do that here!")
    }

    /**
     * An alias to the PluginManager.registerEvent() function that handles some parameters automatically.
     */
    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) =
        plugin.server.pluginManager.registerEvent(event, this, EventPriority.NORMAL, executor,
            plugin, true)
}
