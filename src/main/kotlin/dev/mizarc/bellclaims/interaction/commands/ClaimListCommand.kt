package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import kotlin.math.ceil

@CommandAlias("claimlist")
class ClaimListCommand : BaseCommand() {
    @Dependency
    private lateinit var claimService: ClaimService

    @Default
    @CommandPermission("bellclaims.command.claimlist")
    @CommandCompletion("@nothing @players")
    @Syntax("[count] [player]")
    fun onClaimlist(player: Player, @Default("1") page: Int, @Optional otherPlayer: OfflinePlayer?) {
        val playerClaims = if (otherPlayer != null) {
            claimService.getByPlayer(otherPlayer).toList()
        } else {
            claimService.getByPlayer(player).toList()
        }

        // Check if player has claims
        if (playerClaims.isEmpty()) {
            player.sendMessage("§cThis player has no claims.")
            return
        }

        // Check if page is empty
        if (page * 10 - 9 > playerClaims.count()) {
            player.sendMessage("§cThere are no trusted player entries on that page.")
            return
        }

        // Output list of trusted players
        val chatInfo = ChatInfoBuilder("Claims")
        for (i in 0..9 + page) {
            if (i > playerClaims.count() - 1) {
                break
            }

            val name: String = if (playerClaims[i].name.isEmpty()) playerClaims[i].id.toString().substring(0, 7)
                else playerClaims[i].name
            val blockCount = claimService.getBlockCount(playerClaims[i])
            chatInfo.addLinked(name,
                "<${playerClaims[i].position.x}, ${playerClaims[i].position.y}, ${playerClaims[i].position.z} " +
                        "(${blockCount} Blocks)")
        }
        player.sendMessage(chatInfo.createPaged(page,
            ceil((playerClaims.count() / 10.0)).toInt()))
    }
}