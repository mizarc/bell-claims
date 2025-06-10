package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Syntax
import dev.mizarc.bellclaims.application.actions.player.DoesPlayerHaveClaimOverride
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.GetPartitionByPosition
import dev.mizarc.bellclaims.application.actions.player.tool.GivePlayerClaimTool
import dev.mizarc.bellclaims.application.results.player.DoesPlayerHaveClaimOverrideResult
import dev.mizarc.bellclaims.application.results.player.tool.GivePlayerClaimToolResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ClaimCommand : BaseCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getPartitionByPosition: GetPartitionByPosition by inject()
    private val doesPlayerHaveClaimOverride: DoesPlayerHaveClaimOverride by inject()
    private val getClaimDetails: GetClaimDetails by inject()
    private val givePlayerClaimTool: GivePlayerClaimTool by inject()

    @CommandAlias("claim")
    @CommandPermission("bellclaims.command.claim")
    @Syntax("claim")
    fun onClaim(player: Player) {
        when (givePlayerClaimTool.execute(player.uniqueId)) {
            GivePlayerClaimToolResult.PlayerAlreadyHasTool -> player.sendMessage(localizationProvider.get(
                player.uniqueId, LocalizationKeys.COMMAND_CLAIM_ALREADY_HAVE_TOOL))
            GivePlayerClaimToolResult.Success -> player.sendMessage(localizationProvider.get(
                player.uniqueId, LocalizationKeys.COMMAND_CLAIM_SUCCESS))
            GivePlayerClaimToolResult.PlayerNotFound -> player.sendMessage(localizationProvider.get(
                player.uniqueId, LocalizationKeys.GENERAL_ERROR))
        }
    }

    fun getPartitionAtPlayer(player: Player): Partition? {
        val claimPartition = getPartitionByPosition.execute(player.location.toPosition3D(), player.world.uid)
        if (claimPartition == null) {
            player.sendMessage(localizationProvider.get(
                player.uniqueId, LocalizationKeys.COMMAND_COMMON_UNKNOWN_PARTITION))
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
            player.sendMessage(localizationProvider.get(
                player.uniqueId, LocalizationKeys.COMMAND_COMMON_NO_CLAIM_PERMISSION))
            return false
        }
        return true
    }
}