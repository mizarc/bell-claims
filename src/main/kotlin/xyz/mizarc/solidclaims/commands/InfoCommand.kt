package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
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
    fun onClaiminfo(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return
        }

        val claim = claimPartition.claim
        val name = if (claim.name != null) {
            claim.name
        } else {
            "Claim"
        }

        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())
        val chatInfo = ChatInfoBuilder("$name Summary")
        if (claim.description != null) {
            chatInfo.addParagraph("${claim.description}")
            chatInfo.addSpace()
        }
        chatInfo.addLinked("Owner", claim.owner.name.toString())
        chatInfo.addLinked("Creation Date", dateTimeFormatter.format(claim.creationTime))
        chatInfo.addLinked("Partition Count", claim.claimPartitions.count().toString())
        chatInfo.addLinked("Block Count", claim.getBlockCount().toString())
        chatInfo.addLinked("Trusted Users", claim.playerAccesses.count().toString())
        chatInfo.addSpace()
        chatInfo.addHeader("Current Partition")
        chatInfo.addLinked("First Corner", claimPartition.area.lowerPosition.toString())
        chatInfo.addLinked("Second Corner", claimPartition.area.upperPosition.toString())
        chatInfo.addLinked("Block Count", claimPartition.area.getBlockCount().toString())

        player.spigot().sendMessage(*chatInfo.create())
    }
}