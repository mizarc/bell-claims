package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Syntax
import dev.mizarc.bellclaims.api.*
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.domain.partitions.Partition

open class ClaimCommand : BaseCommand() {
    @Dependency protected lateinit var claimService: ClaimService
    @Dependency protected lateinit var partitionService: PartitionService
    @Dependency protected lateinit var flagService: FlagService
    @Dependency protected lateinit var defaultPermissionService: DefaultPermissionService
    @Dependency protected lateinit var playerPermissionService: PlayerPermissionService
    @Dependency protected lateinit var playerStateService: PlayerStateService

    @CommandAlias("claim")
    @CommandPermission("bellclaims.command.claim")
    @Syntax("claim")
    fun onClaim(player: Player) {
        if (isItemInInventory(player.inventory)) {
            player.sendMessage("§cYou already have the claim tool in your inventory.")
            return
        }

        player.inventory.addItem(getClaimTool())
        player.sendMessage("§aYou have been given the claim tool")
    }

    /**
     * Check if item is already in the player's inventory
     * @param inventory The provided inventory
     * @return True if the item exists in the inventory
     */
    fun isItemInInventory(inventory: PlayerInventory) : Boolean {
        for (item in inventory.contents!!) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimTool().itemMeta) {
                return true
            }
        }
        return false
    }

    fun getPartitionAtPlayer(player: Player): Partition? {
        val claimPartition = partitionService.getByLocation(player.location)
        if (claimPartition == null) {
            player.sendMessage("§cThere is no claim partition at your current location.")
            return null
        }
        return claimPartition
    }

    fun isPlayerHasClaimPermission(player: Player, partition: Partition): Boolean {
        // Check if player state exists
        val playerState = playerStateService.getById(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return false
        }

        // Check if player has override
        if (playerState.claimOverride) {
            return true
        }

        // Check if player owns claim
        val claim = claimService.getById(partition.claimId)!!
        if (player.uniqueId != claim.owner.uniqueId) {
            player.sendMessage("§cYou don't have permission to modify this claim.")
            return false
        }

        return true
    }
}