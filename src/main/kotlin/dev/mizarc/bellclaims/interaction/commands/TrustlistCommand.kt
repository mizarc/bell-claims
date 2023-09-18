package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import kotlin.math.ceil

@CommandAlias("claim")
class TrustlistCommand : ClaimCommand() {

    @Subcommand("trustlist")
    @CommandPermission("bellclaims.command.claim.trustlist")
    fun onTrustlist(player: Player, @Default("1") page: Int) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId)!!
        val trustedPlayers = playerPermissionService.getByClaim(claim).toSortedMap(compareBy { it.uniqueId })

        // Check if claim has no trusted players
        if (trustedPlayers.isEmpty()) {
            player.sendMessage("§cThis claim has no trusted players.")
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > trustedPlayers.count()) {
            player.sendMessage("§cThere are no trusted player entries on that page.")
            return
        }

        // Page 1 includes default permissions alongside 4 player permissions if there are default permissions
        val chatInfo = ChatInfoBuilder("${claim.name} Trusted Players")
        if (page == 1 && defaultPermissionService.getByClaim(claim).isNotEmpty()) {
            chatInfo.addLinked("All Players", defaultPermissionService.getByClaim(claim).toString())

            val entries = trustedPlayers.entries.withIndex().toList().subList(0, 4.coerceAtMost(trustedPlayers.size))
            entries.forEach { (_, entry) ->
                chatInfo.addLinked(
                    Bukkit.getOfflinePlayer(entry.key.uniqueId).name ?: "N/A", entry.value.toString())
            }
            player.sendMessage(chatInfo.createPaged(page, ceil(trustedPlayers.count() / 5.0).toInt()))
            return
        }

        // Output 5 player permissions at a time
        val entries = trustedPlayers.entries.withIndex().toList().subList(0 + ((page - 1) * 5),
            (4 + ((page - 1) * 5)).coerceAtMost(trustedPlayers.size))
        entries.forEach { (_, entry) ->
            chatInfo.addLinked(
                Bukkit.getOfflinePlayer(entry.key.uniqueId).name ?: "N/A", entry.value.toString())
        }
        player.sendMessage(chatInfo.createPaged(page, ceil(trustedPlayers.count() / 5.0).toInt()))
    }
}