package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import kotlin.math.ceil

@CommandAlias("claim")
class PartitionsCommand : ClaimCommand() {
    @Subcommand("partitions")
    @CommandPermission("bellclaims.command.claim.partitions")
    fun onPartitions(player: Player, @Default("1") page: Int) {
        val partition = getPartitionAtPlayer(player) ?: return

        // Check if page is empty
        val claim = claimService.getById(partition.claimId)!!
        val claimPartitions = partitionService.getByClaim(claim).toList()
        if (page * 10 - 9 > claimPartitions.count() || page < 1) {
            player.sendMessage("§cInvalid page specified.")
            return
        }

        // Output list of partitions
        val name = claim.name.ifEmpty { claim.id.toString().substring(0, 7) }
        val chatInfo = ChatInfoBuilder("$name Partitions")
        for (i in 0..9 + page) {
            if (i > claimPartitions.count() - 1) {
                break
            }
            chatInfo.addLinked((i + 1).toString(),
                "Lower (${claimPartitions[i].area.lowerPosition2D.x}), " +
                        "${claimPartitions[i].area.lowerPosition2D.z} | " +
                        "Upper (${claimPartitions[i].area.upperPosition2D.x}), " +
                        "${claimPartitions[i].area.upperPosition2D.z}")
        }

        player.sendMessage(chatInfo.createPaged(page,
            ceil(((partitionService.getByClaim(claim).count()) / 10.0)).toInt()))
    }
}