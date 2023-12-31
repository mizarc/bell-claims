package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class RenameCommand : ClaimCommand() {

    @Subcommand("rename")
    @CommandPermission("bellclaims.command.claim.rename")
    fun onRename(player: Player, name: String) {
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Ensure name is under character limit
        if (name.count() > 32) return player.sendMessage("§cName must be under 32 characters.")

        // Set new name
        val claim = claimService.getById(partition.claimId) ?: return
        claimService.changeName(claim, name)
        player.sendMessage("§aThe name of the claim has been set to §6$name§a.")
    }
}