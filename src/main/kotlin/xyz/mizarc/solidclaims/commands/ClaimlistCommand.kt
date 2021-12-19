package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import xyz.mizarc.solidclaims.SolidClaims
import kotlin.math.ceil

@CommandAlias("claimlist")
class ClaimlistCommand : BaseCommand() {
    @Dependency
    lateinit var plugin: SolidClaims

    @Default
    fun onClaimlist(player: Player, @Default("1") page: Int) {
        // Check if player has claims
        val playerClaims = plugin.playerContainer.getPlayer(player.uniqueId)?.claims
        if (playerClaims == null || playerClaims.isEmpty()) {
            player.sendMessage("This player has no claims.")
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > playerClaims.count()) {
            player.sendMessage("There are no trusted player entries on that page.")
            return
        }

        // Output list of trusted players
        val chatInfo = ChatInfoBuilder("Claim Trusted Players")
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