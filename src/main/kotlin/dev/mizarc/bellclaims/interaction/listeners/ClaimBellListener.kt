package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.services.ClaimService
import dev.mizarc.bellclaims.application.services.ClaimWorldService
import dev.mizarc.bellclaims.application.services.DefaultPermissionService
import dev.mizarc.bellclaims.application.services.FlagService
import dev.mizarc.bellclaims.application.services.PlayerLimitService
import dev.mizarc.bellclaims.application.services.PlayerPermissionService
import dev.mizarc.bellclaims.application.services.PlayerStateService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.menus.ClaimManagementMenu

class ClaimBellListener(private val claimService: ClaimService,
                        private val claimWorldService: ClaimWorldService,
                        private val flagService: FlagService,
                        private val defaultPermissionService: DefaultPermissionService,
                        private val playerPermissionService: PlayerPermissionService,
                        private val playerLimitService: PlayerLimitService,
                        private val playerStateService: PlayerStateService
): Listener {
    @EventHandler
    fun onPlayerClaimHubInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!event.player.isSneaking) return
        val clickedBlock = event.clickedBlock ?: return
        if ((clickedBlock.type) != Material.BELL) return
        if (!event.player.hasPermission("bellclaims.action.bell")) return

        // Check for permission to open bell menu
        val claim = claimWorldService.getByLocation(clickedBlock.location)

        val playerHasTransferRequest = if (claim != null) claimService.playerHasTransferRequest(claim, event.player) else false

        if (claim != null && claim.owner.uniqueId != event.player.uniqueId && !playerHasTransferRequest) {
            event.player.sendActionBar(Component.text("This claim bell is owned by ${claim.owner.name}")
                .color(TextColor.color(255, 85, 85)))
            return
        }

        // Open the menu
        event.isCancelled = true
        val claimBuilder = Claim.Builder(event.player, clickedBlock.location)
        val claimManagementMenu = ClaimManagementMenu(claimService, claimWorldService, flagService, defaultPermissionService,
            playerPermissionService, playerLimitService, playerStateService, claimBuilder)

        if (claim != null && playerHasTransferRequest) {
            claimManagementMenu.openTransferOfferMenu(claim, event.player)
            return
        }

        claimManagementMenu.openClaimManagementMenu()
    }
}