package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.permission.GetClaimPlayerPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GetPlayersWithPermissionInClaim
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil

@CommandAlias("claim")
class TrustListCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getPlayersWithPermissionInClaim: GetPlayersWithPermissionInClaim by inject()
    private val getClaimDetails: GetClaimDetails by inject()
    private val getClaimPlayerPermissions: GetClaimPlayerPermissions by inject()

    @Subcommand("trustlist")
    @CommandPermission("bellclaims.command.claim.trustlist")
    fun onTrustList(player: Player, @Default("1") page: Int) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Get players who have at least one permission in the claim
        val trustedPlayers = getPlayersWithPermissionInClaim.execute(partition.claimId)

        // Notify if claim has no trusted players
        if (trustedPlayers.isEmpty()) {
            player.sendMessage(localizationProvider.get(
                player.uniqueId, LocalizationKeys.COMMAND_CLAIM_TRUST_LIST_NO_PLAYERS))
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > trustedPlayers.count() || page < 1) {
            player.sendMessage(localizationProvider.get(player.uniqueId, LocalizationKeys.COMMAND_COMMON_INVALID_PAGE))
            return
        }

        // Get names and sort alphabetically
        val trustedPlayerInfo = trustedPlayers.map { playerId ->
            val offlinePlayer = Bukkit.getOfflinePlayer(playerId)
            offlinePlayer.let { playerId to it.name }
        }.sortedBy { it.second }

        // Generate chat output header
        val claimName = getClaimDetails.execute(partition.claimId)
        val chatInfo = ChatInfoBuilder(localizationProvider, player.uniqueId, localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_TRUST_LIST_HEADER))

        // Output 5 players at a time per page
        val entries = trustedPlayerInfo.withIndex().toList().subList(0 + ((page - 1) * 5),
            (4 + ((page - 1) * 5)).coerceAtMost(trustedPlayers.size))
        val listSeparator = localizationProvider.get(player.uniqueId, LocalizationKeys.GENERAL_LIST_SEPARATOR)
        entries.forEach { (_, entry) ->
            val permissions = getClaimPlayerPermissions.execute(partition.claimId, entry.first)
            val row = localizationProvider.get(player.uniqueId, LocalizationKeys.COMMAND_CLAIM_TRUST_LIST_ROW,
                entry.second, permissions.joinToString(listSeparator))
            chatInfo.addRow(row)
        }
        player.sendMessage(chatInfo.createPaged(page, ceil(trustedPlayers.count() / 5.0).toInt()))
    }
}