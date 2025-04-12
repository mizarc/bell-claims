package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.RemovePartition
import dev.mizarc.bellclaims.application.results.claim.partition.RemovePartitionResult
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@CommandAlias("claim")
class RemoveCommand : ClaimCommand(), KoinComponent {
    private val removePartition: RemovePartition by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("remove")
    @CommandPermission("bellclaims.command.claim.remove")
    fun onRemove(player: Player) {
        // Get the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Remove flag from the claim and notify player of result
        when (removePartition.execute(partition.id)) {
            RemovePartitionResult.DoesNotExist -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("This partition for claim §6${claimName}§c does not exist.")
            }
            RemovePartitionResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Partition has been removed for claim §6${claimName}§a.")
            }
            RemovePartitionResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}