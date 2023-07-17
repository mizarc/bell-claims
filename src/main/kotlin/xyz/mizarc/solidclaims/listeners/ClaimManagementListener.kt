package xyz.mizarc.solidclaims.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import xyz.mizarc.solidclaims.ClaimService
import xyz.mizarc.solidclaims.claims.*
import xyz.mizarc.solidclaims.menus.ClaimManagementMenu
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.partitions.Position3D

class ClaimManagementListener(private val claimRepository: ClaimRepository,
                              private val partitionRepository: PartitionRepository,
                              private val claimRuleRepository: ClaimRuleRepository,
                              private val claimPermissionRepository: ClaimPermissionRepository,
                              private val playerAccessRepository: PlayerAccessRepository,
                              private val claimService: ClaimService): Listener {

    @EventHandler
    fun onPlayerClaimHubInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!event.player.isSneaking) return
        val clickedBlock = event.clickedBlock ?: return
        if ((clickedBlock.type) != Material.BELL) return

        val claim = claimService.getByLocation(clickedBlock.location)
        if (claim != null && claim.owner.uniqueId != event.player.uniqueId) {
            event.player.sendActionBar(Component.text("This claim bell is owned by ${claim.owner.name}")
                .color(TextColor.color(255, 85, 85)))
            return
        }

        val claimBuilder = Claim.Builder(event.player,
            event.clickedBlock!!.location.world, Position3D(event.clickedBlock!!.location))
        ClaimManagementMenu(claimRepository, partitionRepository, claimPermissionRepository,
            playerAccessRepository, claimRuleRepository, claimService, claimBuilder)
            .openClaimManagementMenu()
    }
}