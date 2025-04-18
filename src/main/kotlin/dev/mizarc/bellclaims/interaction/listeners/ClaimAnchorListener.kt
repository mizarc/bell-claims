package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.transfer.DoesPlayerHaveTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.anchor.GetClaimAnchorAtPosition
import dev.mizarc.bellclaims.application.results.claim.transfer.DoesPlayerHaveTransferRequestResult
import dev.mizarc.bellclaims.application.results.claim.anchor.GetClaimAnchorAtPositionResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.interaction.menus.management.ClaimManagementMenu
import dev.mizarc.bellclaims.interaction.menus.management.ClaimNamingMenu
import dev.mizarc.bellclaims.interaction.menus.management.ClaimTransferMenu
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimAnchorListener(): Listener, KoinComponent {
    private val getClaimAnchorAtPosition: GetClaimAnchorAtPosition by inject()
    private val doesPlayerHaveTransferRequest: DoesPlayerHaveTransferRequest by inject()

    @EventHandler
    fun onPlayerClaimHubInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!event.player.isSneaking) return
        val clickedBlock = event.clickedBlock ?: return
        if ((clickedBlock.type) != Material.BELL) return
        if (!event.player.hasPermission("bellclaims.action.bell")) return

        // Get the claim if it exists at the clicked location
        var claim: Claim? = null
        val claimResult = getClaimAnchorAtPosition.execute(clickedBlock.location.toPosition3D(), clickedBlock.world.uid)
        when (claimResult) {
            is GetClaimAnchorAtPositionResult.Success -> claim = claimResult.claim
            is GetClaimAnchorAtPositionResult.NoClaimAnchorFound -> {}
            is GetClaimAnchorAtPositionResult.StorageError -> {
                event.player.sendMessage("An internal error has occurred, contact your administrator for support.")
                return
            }
        }

        if (claim != null) {
            // Check if player has an active claim transfer request
            var playerHasTransferRequest = false
            val transferResult = doesPlayerHaveTransferRequest.execute(claim.id, event.player.uniqueId)
            when (transferResult) {
                is DoesPlayerHaveTransferRequestResult.Success -> playerHasTransferRequest = transferResult.hasRequest
                else -> {
                    event.player.sendMessage("An internal error has occurred, contact your administrator for support.")
                }
            }

            // Notify no ability to interact with claim without being owner or without an active transfer request
            if (claim.playerId != event.player.uniqueId && !playerHasTransferRequest) {
                val playerName = Bukkit.getOfflinePlayer(claim.playerId)
                event.player.sendActionBar(Component.text("This claim bell is owned by $playerName")
                    .color(TextColor.color(255, 85, 85)))
                return
            }

            // Open transfer request menu if pending
            if (playerHasTransferRequest) {
                val menuNavigator = MenuNavigator(event.player)
                menuNavigator.openMenu(ClaimTransferMenu(menuNavigator, claim, event.player))
                return
            }

            val menuNavigator = MenuNavigator(event.player)
            menuNavigator.openMenu(ClaimManagementMenu(menuNavigator, event.player, claim))
        }

        // Open the menu
        event.isCancelled = true
        val menuNavigator = MenuNavigator(event.player)
        menuNavigator.openMenu(ClaimNamingMenu(event.player, menuNavigator, clickedBlock.location))
    }
}