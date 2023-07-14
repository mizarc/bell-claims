package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


@CommandAlias("claim")
class InfoCommand : ClaimCommand() {

    @Subcommand("info")
    @CommandPermission("solidclaims.command.info")
    fun onClaiminfo(player: Player) {
        val partition = getPartitionAtPlayer(player) ?: return

        val claim = claims.getById(partition.claimId)!!
        val claimPartitions = partitions.getByClaim(claim)
        val blockCount = claimQuery.getBlockCount(claim)
        val name = if (claim.name.isEmpty()) claim.name else claim.id.toString().substring(0, 7)

        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())
        val chatInfo = ChatInfoBuilder("$name Summary")
        if (claim.description.isEmpty()) {
            chatInfo.addParagraph(claim.description)
            chatInfo.addSpace()
        }
        chatInfo.addLinked("Owner", claim.owner.name.toString())
        chatInfo.addLinked("Creation Date", dateTimeFormatter.format(claim.creationTime))
        chatInfo.addLinked("Partition Count", claimPartitions.count().toString())
        chatInfo.addLinked("Block Count", blockCount.toString())
        chatInfo.addLinked("Trusted Users", playerAccessRepository.getByClaim(claim)?.count().toString())
        chatInfo.addSpace()
        chatInfo.addHeader("Current Partition")
        chatInfo.addLinked("First Corner", partition.area.lowerPosition2D.toString())
        chatInfo.addLinked("Second Corner", partition.area.upperPosition2D.toString())
        chatInfo.addLinked("Block Count", partition.area.getBlockCount().toString())

        player.spigot().sendMessage(*chatInfo.create())
    }
}