package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.metadata.UpdateClaimDescription
import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimAttributeResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@CommandAlias("claim")
class DescriptionCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val updateClaimDescription: UpdateClaimDescription by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("description")
    @CommandPermission("bellclaims.command.claim.description")
    fun onDescription(player: Player, description: String) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Assign common variables
        val claimId = partition.claimId
        val playerId = player.uniqueId

        // Update description and notify player of result
        val result = updateClaimDescription.execute(partition.claimId, description)
        val (messageKey, messageArgs) = when (result) {
            is UpdateClaimAttributeResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_DESCRIPTION_SUCCESS,
                arrayOf(getClaimName(playerId, claimId))
            )
            is UpdateClaimAttributeResult.ClaimNotFound -> Pair(
                LocalizationKeys.COMMAND_COMMON_UNKNOWN_CLAIM,
                emptyArray<String>()
            )
            is UpdateClaimAttributeResult.InputTextInvalid -> {
                val firstError = result.errors.firstOrNull()
                when (firstError) {
                    is TextValidationErrorResult.ExceededCharacterLimit -> Pair(
                        LocalizationKeys.COMMAND_CLAIM_DESCRIPTION_EXCEED_LIMIT,
                        arrayOf(name.count().toString(), firstError.maxCharacters)
                    )
                    is TextValidationErrorResult.InvalidCharacters -> Pair(
                        LocalizationKeys.COMMAND_CLAIM_DESCRIPTION_INVALID_CHARACTER,
                        arrayOf(firstError.invalidCharacters)
                    )
                    is TextValidationErrorResult.ContainsBlacklistedWord -> Pair(
                        LocalizationKeys.COMMAND_CLAIM_DESCRIPTION_BLACKLISTED_WORD,
                        arrayOf(firstError.blacklistedWord)
                    )
                    is TextValidationErrorResult.NoCharactersProvided -> Pair(
                        LocalizationKeys.COMMAND_CLAIM_DESCRIPTION_BLANK,
                        emptyArray<String>()
                    )
                    null -> Pair(
                        LocalizationKeys.GENERAL_ERROR,
                        emptyArray<String>()
                    )
                }
            }
            is UpdateClaimAttributeResult.StorageError -> Pair(
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