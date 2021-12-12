package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ChatInfoBuilder
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimPartition
import kotlin.math.absoluteValue

@CommandAlias("Claiminfo")
class ClaiminfoCommand : BaseCommand() {
    @Dependency
    lateinit var plugin: SolidClaims

    @Default
    fun onClaiminfo(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return
        }

        val claim = claimPartition.claim
        val chatInfo = ChatInfoBuilder("Claim Summary")
        chatInfo.addParagraph("lorem ipsum")
        chatInfo.addSpace()
        chatInfo.addLinked("Owner", claim.owner.name.toString())
        chatInfo.addLinked("Creation Date", "123129")
        chatInfo.addLinked("Partition Count", claim.claimPartitions.count().toString())
        chatInfo.addLinked("Block Count", getTotalBlockCount(claim).toString())
        chatInfo.addLinked("Trusted Users", claim.playerAccesses.count().toString())
        chatInfo.addSpace()
        chatInfo.addHeader("Current Partition")
        chatInfo.addLinked("First Corner", claimPartition.firstPosition.toString())
        chatInfo.addLinked("Second Corner", claimPartition.secondPosition.toString())
        chatInfo.addLinked("Block Count", getBlockCount(claimPartition).toString())

        player.spigot().sendMessage(*chatInfo.create())
    }

    private fun getTotalBlockCount(claim: Claim) : Int {
        var count = 0
        for (partition in claim.claimPartitions) {
            count += getBlockCount(partition)
        }
        return count
    }

    private fun getBlockCount(partition: ClaimPartition) : Int {
        return ((partition.secondPosition.first - partition.firstPosition.first + 1) *
                (partition.secondPosition.second - partition.firstPosition.second + 1)).absoluteValue
    }
}