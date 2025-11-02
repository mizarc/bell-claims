package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.GetClaimAtPosition
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition2D
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import dev.mizarc.bellclaims.config.MainConfig

/**
 * Sends an action bar message when a player crosses into or out of a claim.
 */
class ClaimEnterListener: Listener, KoinComponent {
    private val getClaimAtPosition: GetClaimAtPosition by inject()
    private val localizationProvider: LocalizationProvider by inject()
    private val config: MainConfig by inject()

    // Throttle map to prevent excessive checks per player (ms)
    private val lastCheckAt: MutableMap<UUID, Long> = mutableMapOf()
    private val checkThrottleMs: Long = 250L // tuneable: 200-500ms

    // Cache last known claim id per player to avoid spamming action bar
    private val lastClaimForPlayer: MutableMap<UUID, UUID?> = mutableMapOf()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Respect config toggle
        if (!config.showClaimEnterPopup) return

        val to = event.to
        val from = event.from

        // Ignore vertical-only movement and same chunk-level block (only X/Z/world changes matter)
        if (from.world == to.world && from.blockX == to.blockX && from.blockZ == to.blockZ) return

        val player = event.player
        val playerId = player.uniqueId

        // Throttle frequent checks for high-speed movement
        val now = System.currentTimeMillis()
        val last = lastCheckAt[playerId] ?: 0L
        if (now - last < checkThrottleMs) return
        lastCheckAt[playerId] = now

        checkAndNotifyClaimChange(player, to)
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (!config.showClaimEnterPopup) return
        val to = event.to
        checkAndNotifyClaimChange(event.player, to)
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        if (!config.showClaimEnterPopup) return
        checkAndNotifyClaimChange(event.player, event.player.location)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!config.showClaimEnterPopup) return
        // Initialize the player's last known claim so they don't get spammed on first movement
        val player = event.player
        val initialClaim = when (val result = getClaimAtPosition.execute(player.location.world.uid, player.location.toPosition2D())) {
            is GetClaimAtPositionResult.Success -> result.claim
            else -> null
        }
        lastClaimForPlayer[player.uniqueId] = initialClaim?.id
    }

    private fun checkAndNotifyClaimChange(player: Player, location: Location) {
        val playerId = player.uniqueId
        val newClaim: Claim? = when (val result = getClaimAtPosition.execute(location.world.uid, location.toPosition2D())) {
            is GetClaimAtPositionResult.Success -> result.claim
            else -> null
        }

        val newClaimId = newClaim?.id
        val oldClaimId = lastClaimForPlayer[playerId]
        if (oldClaimId == newClaimId) return
        lastClaimForPlayer[playerId] = newClaimId

        if (newClaim == null) {
            val leaveMsg = localizationProvider.get(playerId, LocalizationKeys.FEEDBACK_CLAIM_LEAVE)
            player.sendActionBar(Component.text(leaveMsg).color(TextColor.color(255, 200, 85)))
        } else {
            val name = newClaim.name.ifEmpty { newClaim.id.toString().take(7) }
            val enterMsg = localizationProvider.get(playerId, LocalizationKeys.FEEDBACK_CLAIM_ENTER, name)
            player.sendActionBar(Component.text(enterMsg).color(TextColor.color(85, 255, 165)))
        }
    }
}
