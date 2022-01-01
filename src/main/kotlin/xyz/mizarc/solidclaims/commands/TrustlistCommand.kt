package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import kotlin.math.ceil

@CommandAlias("claim")
class TrustlistCommand : ClaimCommand() {

    @Subcommand("trustlist")
    @CommandPermission("solidclaims.command.trustlist")
    fun onTrustlist(player: Player, @Default("1") page: Int) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("§cThere is no claim partition at your current location.")
            return
        }

        // Check if claim has no trusted players
        val claim = claimPartition.claim
        if (claim.playerAccesses.isEmpty()) {
            player.sendMessage("§cThis claim has no trusted players.")
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > claim.playerAccesses.count()) {
            player.sendMessage("§cThere are no trusted player entries on that page.")
            return
        }

        // Output list of trusted players
        val name = if (claim.name != null) claim.name else claim.id.toString().substring(0, 7)
        val chatInfo = ChatInfoBuilder("$name Trusted Players")
        for (i in 0..9 + page) {
            if (i > claim.playerAccesses.count() - 1) {
                break
            }

            chatInfo.addLinked(Bukkit.getOfflinePlayer(claim.playerAccesses[i].id).name!!,
                claim.playerAccesses[i].claimPermissions.toString())
        }
        player.spigot().sendMessage(*chatInfo.createPaged(page,
            ceil((claim.playerAccesses.count() / 10.0)).toInt()))

    }
}