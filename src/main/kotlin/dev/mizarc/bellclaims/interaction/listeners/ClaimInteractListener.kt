package dev.mizarc.bellclaims.interaction.listeners

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import dev.mizarc.bellclaims.BellClaims
import dev.mizarc.bellclaims.application.services.ClaimService
import dev.mizarc.bellclaims.application.services.DefaultPermissionService
import dev.mizarc.bellclaims.application.services.FlagService
import dev.mizarc.bellclaims.application.services.PartitionService
import dev.mizarc.bellclaims.application.services.PlayerPermissionService
import dev.mizarc.bellclaims.application.services.PlayerStateService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.flags.Flag
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

/**
 * Handles the registration of defined events with their associated actions.
 * @property plugin A reference to the plugin instance
 * @property claimContainer A reference to the ClaimContainer instance
 */
class ClaimInteractListener(private var plugin: BellClaims,
                            private val claimService: ClaimService,
                            private val partitionService: PartitionService,
                            private val flagService: FlagService,
                            private val defaultPermissionService: DefaultPermissionService,
                            private val playerPermissionService: PlayerPermissionService,
                            private val playerStateService: PlayerStateService
) : Listener {
    init {
        for (perm in ClaimPermission.entries) {
            for (e in perm.events) {
                registerEvent(e.eventClass, ::handleClaimPermission)
            }
        }
        for (rule in Flag.entries) {
            for (r in rule.rules) {
                registerEvent(r.eventClass, ::handleClaimRule)
            }
        }
    }

    /**
     * A wrapper function to determine if an event has an appropriate RuleExecutor, and if so, uses it to determine
     * if the event happened inside of claim boundaries, then passes off its handling to the executor if those checks
     * pass.
     */
    private fun handleClaimRule(listener: Listener, event: Event) {
        val rules = Flag.getRulesForEvent(event::class.java).toMutableList() // Get the rules to deal with this event
        val tempExecutor = Flag.getRuleExecutorForEvent(event::class.java) ?: return  // Get the executor that deals with this event
        val claims = tempExecutor.getClaims(event, claimService, partitionService) // Get all claims that this event affects
        if (claims.isEmpty()) return // Check if any claims are affected by the event

        var executor: ((event: Event, claimService: ClaimService,
                        partitionService: PartitionService, flagService: FlagService
        ) -> Boolean)?
        for (claim in claims) { // If they are, check if they do not allow this event
            for (rule in rules) {
                if (!flagService.doesClaimHaveFlag(claim, rule)) {
                    for (ruleExecutor in rule.rules) {
                        if (ruleExecutor.eventClass == event::class.java) {
                            // If they do not, invoke the handler
                            executor = ruleExecutor.handler
                            if (executor.invoke(event, claimService, partitionService, flagService)) {
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * A wrapper function to abstract the business logic of determining if an event occurs within a claim, if the
     * player that the event originated from has permissions within that claim, and if not, which permission event
     * executor has the highest priority, then invoke that executor.
     */
    private fun handleClaimPermission(listener: Listener, event: Event) {
        val eventPerms = ClaimPermission.getPermissionsForEvent(event::class.java).toMutableList() // Get all ClaimPermissions that deal with this event

        // Get the top PermissionExecutor that deals with this event.
        // NOTE: This assumes that any PermissionExecutor that deals with this event will always return the same values
        // for location and player as any other for this event would
        val tempExecutor = ClaimPermission.getPermissionExecutorForEvent(event::class.java) ?: return

        // The player that caused this event, if any
        val player: Player = tempExecutor.source.invoke(event) ?: return

        // Get all claims that this event affects
        val locations = tempExecutor.locations(event)
        val affectedClaims = mutableListOf<Claim>()
        for (location in locations) {
            val partition = partitionService.getByLocation(location) ?: continue
            val claim = claimService.getById(partition.claimId) ?: continue
            affectedClaims.add(claim)
        }
        val claims = affectedClaims.distinct()

        // If player has override, do nothing
        val playerState = playerStateService.getByPlayer(player) ?: run {
            playerStateService.registerPlayer(player)
            playerStateService.getByPlayer(player)
        }
        if (playerState?.claimOverride == true) return

        for (claim in claims) {
            // If player is owner, do nothing.
            if (player.uniqueId == claim.owner.uniqueId) {
                return
            }

            // Get the claim permissions to use, whether it's the trustee's individual permissions, or the claim's default permissions
            var playerPermissions = playerPermissionService.getByPlayer(claim, player)
            if (playerPermissions.isEmpty()) {
                playerPermissions = defaultPermissionService.getByClaim(claim)
            }

            eventPerms.removeAll(playerPermissions)

            var executor: ((l: Listener, e: Event) -> Boolean)? = null // The function that handles the result of this event

            // Determine the highest priority permission for the event and sets the executor to the one found, if any
            for (e in eventPerms) {
                if (!playerPermissions.contains(e)) { // If not, check if it does not contain this permission
                    for (ee in e.events) { // If so, determine the executor to use
                        if (ee.eventClass == event::class.java) {
                            executor = ee.handler
                            // If nothing was executed then the player has permissions to enact this event, so do not send a warning.
                            if (executor.invoke(listener, event)) {
                                player.sendActionBar(
                                    Component.text("You can't do that in ${claim.owner.name}'s claim!")
                                        .color(TextColor.color(255, 85, 85)))
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * An alias to the PluginManager.registerEvent() function that handles some parameters automatically.
     */
    private fun registerEvent(event: Class<out Event>, executor: (l: Listener, e: Event) -> Unit) =
        plugin.server.pluginManager.registerEvent(event, this, EventPriority.LOWEST, executor,
            plugin, true)
}
