package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.api.enums.PartitionDestroyResult
import dev.mizarc.bellclaims.api.events.PartitionModificationEvent
import org.bukkit.entity.Player

@CommandAlias("claim")
class RemoveCommand : ClaimCommand() {
    @Subcommand("remove")
    @CommandPermission("bellclaims.command.claim.remove")
    fun onRemove(player: Player) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        when (partitionService.delete(partition)) {
            PartitionDestroyResult.DISCONNECTED ->
                player.sendMessage("§cCan't remove partition as it would result in a fragmented claim.")
            PartitionDestroyResult.SUCCESS -> {
                player.sendMessage("§aPartition removed for claim §6${claim.name}§a.")
                PartitionModificationEvent(partition).callEvent()
            }
        }
    }
}