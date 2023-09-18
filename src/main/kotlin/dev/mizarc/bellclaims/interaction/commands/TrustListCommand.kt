package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import dev.mizarc.bellclaims.utils.getDisplayName
import kotlin.math.ceil

@CommandAlias("claim")
class TrustListCommand : ClaimCommand() {

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

        // Output 5 player permissions at a time
        val chatInfo = ChatInfoBuilder("${claim.name} Trusted Players")
        val entries = trustedPlayers.entries.withIndex().toList().subList(0 + ((page - 1) * 5),
            (4 + ((page - 1) * 5)).coerceAtMost(trustedPlayers.size))
        entries.forEach { (_, entry) ->
            chatInfo.addLinked(Bukkit.getOfflinePlayer(entry.key.uniqueId).name ?: "N/A",
                entry.value.map { it.getDisplayName() }.toString())
        }
        player.sendMessage(chatInfo.createPaged(page, ceil(trustedPlayers.count() / 5.0).toInt()))
    }
}