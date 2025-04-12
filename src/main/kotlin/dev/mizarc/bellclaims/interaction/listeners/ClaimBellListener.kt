package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.DoesPlayerHaveTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.GetClaimAtPosition
import dev.mizarc.bellclaims.application.results.DoesPlayerHaveTransferRequestResult
import dev.mizarc.bellclaims.application.results.GetClaimAtPositionResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.interaction.menus.ClaimManagementMenuOld
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimBellListener(): Listener, KoinComponent {
    private val getClaimAtPosition: GetClaimAtPosition by inject()
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
        val claimResult = getClaimAtPosition.execute(Position3D(clickedBlock.location), clickedBlock.world.uid)
        when (claimResult) {
            is GetClaimAtPositionResult.Success -> claim = claimResult.claim
            is GetClaimAtPositionResult.NoClaimFound -> {}
            is GetClaimAtPositionResult.StorageError -> {
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
            if (claim.owner.uniqueId != event.player.uniqueId && !playerHasTransferRequest) {
                event.player.sendActionBar(Component.text("This claim bell is owned by ${claim.owner.name}")
                    .color(TextColor.color(255, 85, 85)))
                return
            }

            // Open transfer request menu if pending
            if (playerHasTransferRequest) {
                claimManagementMenu.openTransferOfferMenu(claim, event.player)
                return
            }
        }

        // Open the menu
        event.isCancelled = true
        val claimBuilder = Claim.Builder(event.player, clickedBlock.location)
        val claimManagementMenuOld = ClaimManagementMenuOld(claimService, claimWorldService, flagService, defaultPermissionService,
            playerPermissionService, playerLimitService, playerStateService, claimBuilder)

        claimManagementMenuOld.openClaimManagementMenu()
    }
}