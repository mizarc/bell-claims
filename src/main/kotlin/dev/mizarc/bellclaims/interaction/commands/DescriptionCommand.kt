package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class DescriptionCommand : ClaimCommand() {

    @Subcommand("description")
    @CommandPermission("bellclaims.command.claim.description")
    fun onDescription(player: Player, description: String) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        claimService.changeDescription(claim, description)
        player.sendMessage("§aNew claim description has been set.")
    }
}