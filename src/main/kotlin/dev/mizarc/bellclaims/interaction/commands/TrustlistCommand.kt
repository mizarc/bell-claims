package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import java.util.*
import kotlin.math.ceil

@CommandAlias("claim")
class TrustlistCommand : ClaimCommand() {

    @Subcommand("trustlist")
    @CommandPermission("bellclaims.command.claim.trustlist")
    fun onTrustlist(player: Player, @Default("1") page: Int) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId)!!
        val trustedPlayers = playerPermissionService.getByClaim(claim).toSortedMap(compareBy {it.uniqueId})

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

        // Output list of trusted players
        val chatInfo = ChatInfoBuilder("${claim.name} Trusted Players")

        for ((index, entry) in trustedPlayers.entries.withIndex()) {
            if (index >= trustedPlayers.count()) {
                break
            }

            if (index in 0 + page..9 + page) {
                chatInfo.addLinked(
                    Bukkit.getOfflinePlayer(entry.key.uniqueId).name ?: "N/A", entry.value.toString())
            }
        }

        player.spigot().sendMessage(*chatInfo.createPaged(page, ceil((trustedPlayers.count() / 10.0)).toInt()))
    }
}