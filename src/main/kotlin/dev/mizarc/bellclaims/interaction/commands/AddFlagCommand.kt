package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.results.claim.flags.EnableClaimFlagResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.Flag
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("claim")
class AddFlagCommand : ClaimCommand(), KoinComponent {
    private val enableClaimFlag: EnableClaimFlag by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("addflag")
    @CommandPermission("bellclaims.command.claim.addflag")
    fun onFlag(player: Player, flag: Flag) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Add flag to the claim and notify player of result
        when (enableClaimFlag.execute(flag, partition.claimId)) {
            EnableClaimFlagResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Flag $flag has been added to claim $claimName.")
            }
            EnableClaimFlagResult.AlreadyExists -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Claim $claimName already has $flag.")
            }
            EnableClaimFlagResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            EnableClaimFlagResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}