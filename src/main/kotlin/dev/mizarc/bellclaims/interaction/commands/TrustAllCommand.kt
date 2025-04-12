package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantClaimWidePermission
import dev.mizarc.bellclaims.application.results.claim.permission.GrantClaimWidePermissionResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@CommandAlias("claim")
class TrustAllCommand : ClaimCommand(), KoinComponent {
    private val grantClaimWidePermission: GrantClaimWidePermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("trustall")
    @CommandPermission("bellclaims.command.claim.trustall")
    fun onTrustAll(player: Player, permission: ClaimPermission) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Add permission for player and output result
        when (grantClaimWidePermission.execute(partition.claimId, permission)) {
            GrantClaimWidePermissionResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Permission $permission has been granted claim wide in claim $claimName.")
            }
            GrantClaimWidePermissionResult.AlreadyExists -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Claim $claimName already has $permission granted claim wide.")
            }
            GrantClaimWidePermissionResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            GrantClaimWidePermissionResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}