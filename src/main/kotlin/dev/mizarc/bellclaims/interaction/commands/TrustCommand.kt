package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import dev.mizarc.bellclaims.application.actions.AddClaimPlayerPermission
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.results.AddClaimPlayerPermissionResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("claim")
class TrustCommand : ClaimCommand(), KoinComponent {
    private val addClaimPlayerPermission: AddClaimPlayerPermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("trust")
    @CommandPermission("bellclaims.command.claim.trust")
    fun onTrust(player: Player, otherPlayer: OnlinePlayer, permission: ClaimPermission) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Add permission for player and output result
        when (addClaimPlayerPermission.execute(partition.claimId, otherPlayer.player.uniqueId, permission)) {
            AddClaimPlayerPermissionResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("Permission $permission has been assigned to player " +
                        "${otherPlayer.player.displayName()} in claim $claimName.")
            }
            AddClaimPlayerPermissionResult.AlreadyExists -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("${otherPlayer.player.displayName()} already has $permission " +
                        "permissions in claim $claimName.")
            }
            AddClaimPlayerPermissionResult.ClaimNotFound ->
                player.sendMessage("Claim was not found.")
            AddClaimPlayerPermissionResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}