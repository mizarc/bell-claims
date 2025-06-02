package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokeClaimWidePermission
import dev.mizarc.bellclaims.application.results.claim.permission.RevokeClaimWidePermissionResult
import dev.mizarc.bellclaims.application.results.claim.permission.RevokePlayerClaimPermissionResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@CommandAlias("claim")
class UntrustAllCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val revokeClaimWidePermission: RevokeClaimWidePermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("untrustall")
    @CommandPermission("bellclaims.command.claim.untrustall")
    fun onUntrustAll(player: Player, permission: ClaimPermission) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Assign common variables
        val claimId = partition.claimId
        val playerId = player.uniqueId

        // Execute action to revoke claim wide permissions and fetch associated locale text
        val outcome = revokeClaimWidePermission.execute(claimId, permission)
        val (messageKey, messageArgs) = when (outcome) {
            is RevokeClaimWidePermissionResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_UNTRUST_ALL_SUCCESS,
                arrayOf(getPermissionName(playerId, permission), getClaimName(playerId, claimId))
            )
            is RevokeClaimWidePermissionResult.DoesNotExist -> Pair(
                LocalizationKeys.COMMAND_CLAIM_UNTRUST_ALL_DOES_NOT_EXIST,
                arrayOf(getClaimName(playerId, claimId), getPermissionName(playerId, permission))
            )
            is RevokeClaimWidePermissionResult.ClaimNotFound -> Pair(
                LocalizationKeys.COMMAND_COMMON_UNKNOWN_CLAIM,
                emptyArray<String>()
            )
            is RevokeClaimWidePermissionResult.StorageError -> Pair(
                LocalizationKeys.GENERAL_ERROR,
                emptyArray<String>()
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

    /**
     * Helper function to retrieve the name of the permission.
     */
    private fun getPermissionName(playerId: UUID, permission: ClaimPermission): String {
        return localizationProvider.get(playerId, permission.nameKey)
    }
}