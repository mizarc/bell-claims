package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.RemoveFlagFromClaim
import dev.mizarc.bellclaims.application.enums.RemoveFlagFromClaimResult
import dev.mizarc.bellclaims.application.results.AddFlagToClaimResult
import dev.mizarc.bellclaims.application.results.FlagChangeResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.Flag
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("claim")
class RemoveFlagCommand : ClaimCommand(), KoinComponent {
    private val removeFlagFromClaim: RemoveFlagFromClaim by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("removeflag")
    @CommandPermission("bellclaims.command.claim.removeflag")
    fun onRemoveFlag(player: Player, flag: Flag) {
        // Get the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Remove flag from the claim and notify player of result
        when (removeFlagFromClaim.execute(flag, partition.claimId)) {
            RemoveFlagFromClaimResult.DoesNotExist -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Claim §6${claimName}§c does not have §6$flag.")
            }
            RemoveFlagFromClaimResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("§6$flag §adisabled for claim §6${claimName}§a.")
            }
            RemoveFlagFromClaimResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            RemoveFlagFromClaimResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}