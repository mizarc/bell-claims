package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.Claim
import kotlin.math.ceil

@CommandAlias("claimlist")
class ClaimlistCommand : BaseCommand() {
    @Dependency
    lateinit var plugin: SolidClaims

    @Default
    @CommandCompletion("@nothing @players")
    @Syntax("[count] [player]")
    fun onClaimlist(player: Player, @Default("1") page: Int, @Optional otherPlayer: OfflinePlayer?) {
        val playerClaims: ArrayList<Claim>
        if (otherPlayer != null) {
            playerClaims = plugin.playerContainer.getPlayer(otherPlayer.uniqueId)?.claims!!
        }
        else {
            playerClaims = plugin.playerContainer.getPlayer(player.uniqueId)?.claims!!
        }

        // Check if player has claims
        if (playerClaims.isEmpty()) {
            player.sendMessage("This player has no claims.")
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > playerClaims.count()) {
            player.sendMessage("There are no trusted player entries on that page.")
            return
        }

        // Output list of trusted players
        val chatInfo = ChatInfoBuilder("Claims")
        for (i in 0..9 + page) {
            if (i > playerClaims.count() - 1) {
                break
            }
            chatInfo.addLinked(playerClaims[i].id.toString().substring(0, 7),
                "<${(playerClaims[i].mainPartition!!.firstPosition.first + 
                        playerClaims[i].mainPartition!!.secondPosition.first) / 2}, " +
                        "${(playerClaims[i].mainPartition!!.firstPosition.second +
                        playerClaims[i].mainPartition!!.secondPosition.second) / 2}>"
                )
        }
        player.spigot().sendMessage(*chatInfo.createPaged(page,
            ceil((playerClaims.count() / 10.0)).toInt()))
    }
}