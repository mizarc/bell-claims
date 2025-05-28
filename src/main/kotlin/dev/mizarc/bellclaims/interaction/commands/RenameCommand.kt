package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.metadata.UpdateClaimName
import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimAttributeResult
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimNameResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.getValue

@CommandAlias("claim")
class RenameCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val updateClaimName: UpdateClaimName by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("rename")
    @CommandPermission("bellclaims.command.claim.rename")
    fun onRename(player: Player, name: String) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Assign common variables
        val claimId = partition.claimId
        val playerId = player.uniqueId

        // Update name and notify player of result
        val result = updateClaimName.execute(partition.claimId, name)
        when (result) {
            is UpdateClaimNameResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_RENAME_SUCCESS,
                arrayOf(getClaimName(playerId, claimId), name)
            )
            is UpdateClaimNameResult.NameAlreadyExists -> Pair(
                LocalizationKeys.COMMAND_CLAIM_RENAME_ALREADY_EXISTS,
                arrayOf(name)
            )
            is UpdateClaimNameResult.ClaimNotFound -> Pair(
                LocalizationKeys.COMMAND_COMMON_UNKNOWN_CLAIM,
                emptyArray<String>()
            )
            is UpdateClaimNameResult.InputTextInvalid -> {
                result.errors.forEach { error ->
                    when (error) {
                        is TextValidationErrorResult.ExceededCharacterLimit -> Pair(
                            LocalizationKeys.COMMAND_CLAIM_RENAME_EXCEED_LIMIT,
                            arrayOf(name.count().toString(), error.maxCharacters)
                        )
                        is TextValidationErrorResult.InvalidCharacters -> Pair(
                            LocalizationKeys.COMMAND_CLAIM_RENAME_INVALID_CHARACTER,
                            arrayOf(error.invalidCharacters)
                        )
                        is TextValidationErrorResult.ContainsBlacklistedWord -> Pair(
                            LocalizationKeys.COMMAND_CLAIM_RENAME_BLACKLISTED_WORD,
                            arrayOf(error.blacklistedWord)
                        )
                        is TextValidationErrorResult.NoCharactersProvided -> Pair(
                            LocalizationKeys.COMMAND_CLAIM_RENAME_BLANK,
                            emptyArray<String>()
                        )
                    }
                }
            }
            is UpdateClaimNameResult.StorageError -> Pair(
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
}