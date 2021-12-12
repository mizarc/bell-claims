package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import xyz.mizarc.solidclaims.ChatInfoBuilder
import xyz.mizarc.solidclaims.SolidClaims

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
        chatInfo.addLinked("Blocks", "1")
        chatInfo.addLinked("Trusted Users", "7")

        player.spigot().sendMessage(*chatInfo.create())
    }
}