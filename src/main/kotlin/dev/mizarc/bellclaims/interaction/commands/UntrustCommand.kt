package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.permissions.RevokePlayerClaimPermission
import dev.mizarc.bellclaims.application.results.RevokePlayerClaimPermissionResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@CommandAlias("claim")
class UntrustCommand : ClaimCommand(), KoinComponent {
    private val revokePlayerClaimPermission: RevokePlayerClaimPermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("untrust")
    @CommandPermission("bellclaims.command.claim.untrust")
    fun onTrust(player: Player, targetPlayer: OnlinePlayer, permission: ClaimPermission) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Add permission for player and output result
        when (revokePlayerClaimPermission.execute(partition.claimId, targetPlayer.player.uniqueId, permission)) {
            RevokePlayerClaimPermissionResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Permission $permission has been revoked from player " +
                        "${targetPlayer.player.displayName()} in claim $claimName.")
            }
            RevokePlayerClaimPermissionResult.DoesNotExist -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("${targetPlayer.player.displayName()} does not have $permission " +
                        "permissions in claim $claimName.")
            }
            RevokePlayerClaimPermissionResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            RevokePlayerClaimPermissionResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}