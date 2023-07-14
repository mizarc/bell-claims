package xyz.mizarc.solidclaims.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import xyz.mizarc.solidclaims.claims.*
import xyz.mizarc.solidclaims.menus.ClaimManagementMenu
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.partitions.Position3D

class ClaimManagementListener(private val claimRepository: ClaimRepository,
                              private val partitionRepository: PartitionRepository,
                              private val claimRuleRepository: ClaimRuleRepository,
                              private val claimPermissionRepository: ClaimPermissionRepository,
                              private val playerAccessRepository: PlayerAccessRepository): Listener {

    @EventHandler
    fun onPlayerClaimObjectInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if ((event.clickedBlock?.type ?: return) != Material.BELL) return

        if (event.player.isSneaking) {
            val claimBuilder = Claim.Builder(event.player,
                event.clickedBlock!!.location.world, Position3D(event.clickedBlock!!.location))
            ClaimManagementMenu(claimRepository, partitionRepository, claimPermissionRepository,
                playerAccessRepository, claimRuleRepository, claimBuilder)
                .openClaimManagementMenu()
        }
    }
}