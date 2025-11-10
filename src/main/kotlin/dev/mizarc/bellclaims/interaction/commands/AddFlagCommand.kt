package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.results.claim.flags.EnableClaimFlagResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.values.Flag
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@CommandAlias("claim")
class AddFlagCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val enableClaimFlag: EnableClaimFlag by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("addflag")
    @CommandPermission("bellclaims.command.claim.addflag")
    fun onAddFlag(player: Player, flag: Flag) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Assign common variables
        val claimId = partition.claimId
        val playerId = player.uniqueId

        // Add flag to the claim and notify player of result
        val (messageKey, messageArgs) = when (enableClaimFlag.execute(flag, partition.claimId)) {
            EnableClaimFlagResult.Success -> Pair(
                LocalizationKeys.COMMAND_CLAIM_ADD_FLAG_SUCCESS,
                arrayOf(getFlagName(playerId, flag), getClaimName(playerId, claimId))
            )
            EnableClaimFlagResult.AlreadyExists -> Pair(
                LocalizationKeys.COMMAND_CLAIM_ADD_FLAG_ALREADY_EXISTS,
                arrayOf(getClaimName(playerId, claimId), getFlagName(playerId, flag))
            )
            EnableClaimFlagResult.ClaimNotFound -> Pair(
                LocalizationKeys.COMMAND_COMMON_UNKNOWN_CLAIM,
                emptyArray<String>()
            )
            EnableClaimFlagResult.StorageError -> Pair(
                LocalizationKeys.GENERAL_ERROR,
                emptyArray<String>()
            )
            EnableClaimFlagResult.Blacklisted -> Pair(
                LocalizationKeys.COMMAND_CLAIM_ADD_FLAG_BLACKLISTED,
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
    private fun getFlagName(playerId: UUID, flag: Flag): String {
        return localizationProvider.get(playerId, flag.nameKey)
    }
}