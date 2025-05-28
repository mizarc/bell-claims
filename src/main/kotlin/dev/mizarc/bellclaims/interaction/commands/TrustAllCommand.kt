package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantClaimWidePermission
import dev.mizarc.bellclaims.application.results.claim.permission.GrantClaimWidePermissionResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.getValue

@CommandAlias("claim")
class TrustAllCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val grantClaimWidePermission: GrantClaimWidePermission by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("trustall")
    @CommandPermission("bellclaims.command.claim.trustall")
    fun onTrustAll(player: Player, permission: ClaimPermission) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Assign common variables
        val claimId = partition.claimId
        val playerId = player.uniqueId

        // Add permission for player and output result
        when (grantClaimWidePermission.execute(partition.claimId, permission)) {
            is GrantClaimWidePermissionResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_TRUST_ALL_SUCCESS,
                arrayOf(getPermissionName(playerId, permission), getClaimName(playerId, claimId))
            )
            is GrantClaimWidePermissionResult.AlreadyExists -> Pair(
                LocalizationKeys.COMMAND_CLAIM_TRUST_ALL_ALREADY_EXISTS,
                arrayOf(getClaimName(playerId, claimId), getPermissionName(playerId, permission))
            )
            is GrantClaimWidePermissionResult.ClaimNotFound -> Pair(
                LocalizationKeys.COMMAND_COMMON_UNKNOWN_CLAIM,
                emptyArray<String>()
            )
            is GrantClaimWidePermissionResult.StorageError -> Pair(
                LocalizationKeys.GENERAL_ERROR,
                emptyArray<String>()
            )
        }
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