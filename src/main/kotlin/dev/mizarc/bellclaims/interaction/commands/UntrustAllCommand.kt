package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.RevokeClaimWidePermission
import dev.mizarc.bellclaims.application.results.RevokeClaimWidePermissionResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("claim")
class UntrustAllCommand : ClaimCommand(), KoinComponent {
    private val revokeClaimWidePermission: RevokeClaimWidePermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("untrustall")
    @CommandPermission("bellclaims.command.claim.untrustall")
    fun onUntrustAll(player: Player, permission: ClaimPermission) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Add permission for player and output result
        when (revokeClaimWidePermission.execute(partition.claimId, permission)) {
            RevokeClaimWidePermissionResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Permission $permission has been revoked claim wide in claim $claimName.")
            }
            RevokeClaimWidePermissionResult.DoesNotExist -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Claim $claimName does not have $permission granted claim wide.")
            }
            RevokeClaimWidePermissionResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            RevokeClaimWidePermissionResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}