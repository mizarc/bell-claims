package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import xyz.mizarc.solidclaims.SolidClaims
import kotlin.math.absoluteValue
import kotlin.math.ceil

@CommandAlias("claim")
class TrustlistCommand : ClaimCommand() {

    @Subcommand("trustlist")
    fun onTrustlist(player: Player, page: Int) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return
        }

        // Output list of trusted players
        val claim = claimPartition.claim
        val chatInfo = ChatInfoBuilder("Claim Trusted Players")
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