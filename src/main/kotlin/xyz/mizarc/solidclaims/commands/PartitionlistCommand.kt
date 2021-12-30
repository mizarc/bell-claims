package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
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
        if (page * 10 - 9 > claim.partitions.count()) {
            player.sendMessage("There are no claim partitions on that page.")
            return
        }

        // Output list of partitions
        val name = if (claim.name != null) claim.name else claim.id.toString().substring(0, 7)
        val chatInfo = ChatInfoBuilder("$name Partitions")
        for (i in 0..9 + page) {
            if (i > claim.partitions.count() - 1) {
                break
            }

            chatInfo.addLinked((i + 1).toString(),
                "${claim.partitions[i].area.lowerPosition} ${claim.partitions[i].area.upperPosition}")
        }
        player.spigot().sendMessage(*chatInfo.createPaged(page,
            ceil((claim.playerAccesses.count() / 10.0)).toInt()))
    }
}