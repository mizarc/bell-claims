package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import xyz.mizarc.solidclaims.ClaimQuery
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimRepository
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.players.PlayerStateRepository
import kotlin.math.ceil

@CommandAlias("claimlist")
class ClaimlistCommand : BaseCommand() {
    @Dependency
    lateinit var claims: ClaimRepository
    lateinit var partitions: PartitionRepository
    lateinit var playerStates: PlayerStateRepository
    protected lateinit var claimQuery: ClaimQuery

    @Default
    @CommandPermission("solidclaims.command.claimlist")
    @CommandCompletion("@nothing @players")
    @Syntax("[count] [player]")
    fun onClaimlist(player: Player, @Default("1") page: Int, @Optional otherPlayer: OfflinePlayer?) {
        val playerClaims = if (otherPlayer != null) {
            claims.getByPlayer(otherPlayer.uniqueId).toList()
        } else {
            claims.getByPlayer(player.uniqueId).toList()
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
            val blockCount = claimQuery.getBlockCount(playerClaims[i])

            val mainPartition = partitions.getById(playerClaims[i].mainPartitionId)
            chatInfo.addLinked(name,
                "<${(mainPartition!!.area.lowerPosition.x + mainPartition.area.upperPosition.x) / 2}, " +
                        "${(mainPartition.area.lowerPosition.z + mainPartition.area.upperPosition.z) / 2}> " +
                        "(${blockCount} Blocks)")
        }
        player.spigot().sendMessage(*chatInfo.createPaged(page,
            ceil((playerClaims.count() / 10.0)).toInt()))
    }
}