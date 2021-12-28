package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import kotlin.math.ceil

@CommandAlias("claim")
class PartitionlistCommand : ClaimCommand() {

    @Subcommand("partitionlist")
    fun onPartitionlist(player: Player, @Default("1") page: Int) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return
        }

        // Check if page is empty
        val claim = claimPartition.claim
        if (page * 10 - 9 > claim.claimPartitions.count()) {
            player.sendMessage("There are no claim partitions on that page.")
            return
        }

        // Output list of partitions
        val chatInfo = ChatInfoBuilder("Claim Partitions")
        for (i in 0..9 + page) {
            if (i > claim.claimPartitions.count() - 1) {
                break
            }

            chatInfo.addLinked((i + 1).toString(),
                "${claim.claimPartitions[i].area.lowerPosition} ${claim.claimPartitions[i].area.upperPosition}")
        }
        player.spigot().sendMessage(*chatInfo.createPaged(page,
            ceil((claim.playerAccesses.count() / 10.0)).toInt()))
    }
}