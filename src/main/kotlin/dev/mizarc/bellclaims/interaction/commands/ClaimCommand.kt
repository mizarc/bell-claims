package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Syntax
import dev.mizarc.bellclaims.application.actions.player.DoesPlayerHaveClaimOverride
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.GetPartitionByPosition
import dev.mizarc.bellclaims.application.results.player.DoesPlayerHaveClaimOverrideResult
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position3D

import dev.mizarc.bellclaims.utils.getLangText
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ClaimCommand : BaseCommand(), KoinComponent {
    private val getPartitionByPosition: GetPartitionByPosition by inject()
    private val doesPlayerHaveClaimOverride: DoesPlayerHaveClaimOverride by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @CommandAlias("claim")
    @CommandPermission("bellclaims.command.claim")
    @Syntax("claim")
    fun onClaim(player: Player) {
        if (isItemInInventory(player.inventory)) {
            player.sendMessage(getLangText("AlreadyHaveClaimTool"))
            return
        }

        player.inventory.addItem(getClaimTool())
        player.sendMessage(getLangText("ClaimToolGiven"))
    }

    /**
     * Check if item is already in the player's inventory
     * @param inventory The provided inventory
     * @return True if the item exists in the inventory
     */
    fun isItemInInventory(inventory: PlayerInventory) : Boolean {
        for (item in inventory.contents) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimTool().itemMeta) {
                return true
            }
        }
        return false
    }

    fun getPartitionAtPlayer(player: Player): Partition? {
        val claimPartition = getPartitionByPosition.execute(Position3D(player.location), player.world.uid)
        if (claimPartition == null) {
            player.sendMessage(getLangText("NoClaimPartitionHere"))
            return null
        }
        return claimPartition
    }

    fun isPlayerHasClaimPermission(player: Player, partition: Partition): Boolean {
        // Check if player has override
        val overrideResult = doesPlayerHaveClaimOverride.execute(player.uniqueId)
        when (overrideResult) {
            is DoesPlayerHaveClaimOverrideResult.Success -> if (overrideResult.hasOverride) return true
            is DoesPlayerHaveClaimOverrideResult.StorageError -> return false
        }

        // Check if player owns claim
        val claim = getClaimDetails.execute(partition.claimId) ?: return false
        if (player.uniqueId != claim.playerId) {
            player.sendMessage(getLangText("NoPermissionToModifyClaim"))
            return false
        }
        return true
    }
}