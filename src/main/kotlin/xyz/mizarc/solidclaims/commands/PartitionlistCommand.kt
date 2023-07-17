package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import kotlin.math.ceil

@CommandAlias("claim")
class PartitionlistCommand : ClaimCommand() {

    @Subcommand("partitionlist")
    @CommandPermission("solidclaims.command.partitionlist")
    fun onPartitionlist(player: Player, @Default("1") page: Int) {
        val partition = getPartitionAtPlayer(player) ?: return

        // Check if page is empty
        val claim = claims.getById(partition.claimId)!!
        val claimPartitions = partitions.getByClaim(claim)
        if (page * 10 - 9 > claimPartitions.count()) {
            player.sendMessage("§cThere are no claim partitions on that page.")
            return
        }

        // Output list of partitions
        val name = if (claim.name.isNotEmpty()) claim.name else claim.id.toString().substring(0, 7)
        val chatInfo = ChatInfoBuilder("$name Partitions")
        for (i in 0..9 + page) {
            if (i > claimPartitions.count() - 1) {
                break
            }
            chatInfo.addLinked((i + 1).toString(),
                "${claimPartitions[i].area.lowerPosition2D} ${claimPartitions[i].area.upperPosition2D}")
        }

        player.spigot().sendMessage(*chatInfo.createPaged(page,
            ceil(((playerAccessRepository.getByClaim(claim)?.count() ?: 0) / 10.0)).toInt()))
    }
}