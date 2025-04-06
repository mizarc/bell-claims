package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.GetClaimPartitions
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil

@CommandAlias("claim")
class PartitionsCommand : ClaimCommand(), KoinComponent {
    private val getClaimPartitions: GetClaimPartitions by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("partitions")
    @CommandPermission("bellclaims.command.claim.partitions")
    fun onPartitions(player: Player, @Default("1") page: Int) {
        // Get the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return

        // Get partitions of claim
        val partitions = getClaimPartitions.execute(partition.claimId)

        // Check if page is empty
        if (page * 10 - 9 > partitions.count() || page < 1) {
            player.sendMessage("§cInvalid page specified.")
            return
        }

        // Output list of partitions
        val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
        val chatInfo = ChatInfoBuilder("$claimName Partitions")
        for (i in 0..9 + page) {
            if (i > partitions.count() - 1) {
                break
            }
            chatInfo.addLinked((i + 1).toString(),
                "Lower (${partitions[i].area.lowerPosition2D.x}), " +
                        "${partitions[i].area.lowerPosition2D.z} | " +
                        "Upper (${partitions[i].area.upperPosition2D.x}), " +
                        "${partitions[i].area.upperPosition2D.z}")
        }
        player.sendMessage(chatInfo.createPaged(page, ceil((partitions.count() / 10.0)).toInt()))
    }
}