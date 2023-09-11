package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.*
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
                        private val playerLimitService: PlayerLimitService): Listener {
    @EventHandler
    fun onPlayerClaimHubInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!event.player.isSneaking) return
        val clickedBlock = event.clickedBlock ?: return
        if ((clickedBlock.type) != Material.BELL) return
        if (!event.player.hasPermission("bellclaims.action.bell")) return

        // Check for permission to open bell menu
        val claim = claimWorldService.getByLocation(clickedBlock.location)
        if (claim != null && claim.owner.uniqueId != event.player.uniqueId) {
            event.player.sendActionBar(Component.text("This claim bell is owned by ${claim.owner.name}")
                .color(TextColor.color(255, 85, 85)))
            return
        }

        // Open the menu
        val claimBuilder = Claim.Builder(event.player, event.clickedBlock!!.location)
        ClaimManagementMenu(claimService, claimWorldService, flagService, defaultPermissionService,
            playerPermissionService, playerLimitService, claimBuilder).openClaimManagementMenu()
    }
}