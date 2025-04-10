package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.GetClaimPlayerPermissions
import dev.mizarc.bellclaims.application.actions.GetPlayersWithPermissionInClaim
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil

@CommandAlias("claim")
class TrustListCommand : ClaimCommand(), KoinComponent {
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
            player.sendMessage("§cThis claim has no trusted players.")
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > trustedPlayers.count() || page < 1) {
            player.sendMessage("§cInvalid page specified.")
            return
        }

        // Get names and sort alphabetically
        val trustedPlayerInfo = trustedPlayers.map { playerId ->
            val offlinePlayer = Bukkit.getOfflinePlayer(playerId)
            offlinePlayer.let { playerId to it.name }
        }.sortedBy { it.second }

        // Generate chat output header
        val claimName = getClaimDetails.execute(partition.claimId)
        val chatInfo = ChatInfoBuilder("$claimName Trusted Players")

        // Output 5 players at a time per page
        val entries = trustedPlayerInfo.withIndex().toList().subList(0 + ((page - 1) * 5),
            (4 + ((page - 1) * 5)).coerceAtMost(trustedPlayers.size))
        entries.forEach { (_, entry) ->
            val permissions = getClaimPlayerPermissions.execute(partition.claimId, entry.first)
            chatInfo.addLinked(entry.second ?: "N/A", permissions.joinToString(", "))
        }
        player.sendMessage(chatInfo.createPaged(page, ceil(trustedPlayers.count() / 5.0).toInt()))
    }
}