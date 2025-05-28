package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.RemovePartition
import dev.mizarc.bellclaims.application.results.claim.partition.RemovePartitionResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.getValue

@CommandAlias("claim")
class RemoveCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val removePartition: RemovePartition by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("remove")
    @CommandPermission("bellclaims.command.claim.remove")
    fun onRemove(player: Player) {
        // Get the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Assign common variables
        val claimId = partition.claimId
        val playerId = player.uniqueId

        // Remove flag from the claim and notify player of result
        val (messageKey, messageArgs) = when (removePartition.execute(partition.id)) {
            is RemovePartitionResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_REMOVE_SUCCESS,
                arrayOf(getClaimName(playerId, claimId))
            )
            RemovePartitionResult.DoesNotExist -> Pair(
                LocalizationKeys.COMMAND_CLAIM_REMOVE_UNKNOWN_PARTITION,
                arrayOf(getClaimName(playerId, claimId))
            )
            RemovePartitionResult.Disconnected -> Pair(
                LocalizationKeys.COMMAND_CLAIM_REMOVE_DISCONNECTED,
                emptyArray<String>()
            )
            RemovePartitionResult.ExposedClaimAnchor -> Pair(
                LocalizationKeys.COMMAND_CLAIM_REMOVE_EXPOSED_ANCHOR,
                emptyArray<String>()
            )
            RemovePartitionResult.StorageError -> Pair(
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
}