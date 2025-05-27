package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokePlayerClaimPermission
import dev.mizarc.bellclaims.application.results.claim.permission.RevokePlayerClaimPermissionResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.getValue

@CommandAlias("claim")
class UntrustCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val revokePlayerClaimPermission: RevokePlayerClaimPermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("untrust")
    @CommandPermission("bellclaims.command.claim.untrust")
    fun onUntrust(player: Player, targetPlayer: OnlinePlayer, permission: ClaimPermission) {
        // Get the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Execute action to revoke permission and fetch associated locale text
        val outcome = revokePlayerClaimPermission.execute(partition.claimId, targetPlayer.player.uniqueId, permission)
        val (messageKey, messageArgs) = when (outcome) {
            is RevokePlayerClaimPermissionResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_UNTRUST_SUCCESS,
                arrayOf(targetPlayer.player.displayName(), getClaimName(player.uniqueId, partition.claimId))
            )
            is RevokePlayerClaimPermissionResult.DoesNotExist -> Pair(
                LocalizationKeys.COMMAND_CLAIM_UNTRUST_DOES_NOT_EXIST,
                arrayOf(permission, targetPlayer.player.displayName(), getClaimName(player.uniqueId, partition.claimId))
            )
            is RevokePlayerClaimPermissionResult.ClaimNotFound -> Pair(
                LocalizationKeys.COMMAND_COMMON_UNKNOWN_CLAIM,
                emptyArray()
            )
            is RevokePlayerClaimPermissionResult.StorageError -> Pair(
                LocalizationKeys.GENERAL_ERROR,
                emptyArray()
            )
        }

        // Output to player chat
        player.sendMessage(localizationProvider.get(player.uniqueId, messageKey, *messageArgs))
    }

    /**
     * Helper function to retrieve the claim name or a default error message if not found.
     */
    private fun getClaimName(playerId: UUID, claimId: UUID): String {
        return getClaimDetails.execute(claimId)?.name ?: localizationProvider.get(
            playerId, LocalizationKeys.GENERAL_NAME_ERROR
        )
    }
}