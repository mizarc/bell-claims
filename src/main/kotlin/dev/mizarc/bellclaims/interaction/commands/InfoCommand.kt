package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import dev.mizarc.bellclaims.utils.getDisplayName
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


@CommandAlias("claim")
class InfoCommand : ClaimCommand() {

    @Subcommand("info")
    @CommandPermission("bellclaims.command.claim.info")
    fun onClaimInfo(player: Player) {
        val partition = getPartitionAtPlayer(player) ?: return

        val claim = claimService.getById(partition.claimId)!!
        val claimPartitions = partitionService.getByClaim(claim)
        val blockCount = claimService.getBlockCount(claim)

        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())
        val chatInfo = ChatInfoBuilder("${claim.name} Summary")
        chatInfo.addLinked("Owner", claim.owner.name.toString())
        chatInfo.addLinked("Creation Date", dateTimeFormatter.format(claim.creationTime))
        chatInfo.addLinked("Partition Count", claimPartitions.count().toString())
        chatInfo.addLinked("Block Count", blockCount.toString())
        chatInfo.addLinked("Flags", flagService.getByClaim(claim).map { it.getDisplayName() }.toString())
        chatInfo.addLinked("Default Permissions",
            defaultPermissionService.getByClaim(claim).map { it.getDisplayName() }.toString())
        chatInfo.addLinked("Trusted Users", playerPermissionService.getByClaim(claim).count().toString())
        chatInfo.addSpace()
        chatInfo.addHeader("Current Partition")
        chatInfo.addLinked("Lower Corner", "${partition.area.lowerPosition2D.x}, " +
                "${partition.area.lowerPosition2D.z}")
        chatInfo.addLinked("Upper Corner", "${partition.area.upperPosition2D.x}, " +
                "${partition.area.upperPosition2D.z}")
        chatInfo.addLinked("Block Count", partition.area.getBlockCount().toString())

        player.sendMessage(chatInfo.create())
    }
}