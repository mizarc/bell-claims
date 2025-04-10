package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.flags.DisableClaimFlag
import dev.mizarc.bellclaims.application.enums.DisableClaimFlagResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.Flag
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("claim")
class RemoveFlagCommand : ClaimCommand(), KoinComponent {
    private val disableClaimFlag: DisableClaimFlag by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("removeflag")
    @CommandPermission("bellclaims.command.claim.removeflag")
    fun onRemoveFlag(player: Player, flag: Flag) {
        // Get the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Remove flag from the claim and notify player of result
        when (disableClaimFlag.execute(flag, partition.claimId)) {
            DisableClaimFlagResult.DoesNotExist -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Claim §6${claimName}§c does not have §6$flag.")
            }
            DisableClaimFlagResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("§6$flag §adisabled for claim §6${claimName}§a.")
            }
            DisableClaimFlagResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            DisableClaimFlagResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}