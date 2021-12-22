package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims


@CommandAlias("unclaim")
class UnclaimCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @PreCommand
    fun preCommand(player: Player): Boolean {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return true
        }

        // Check if player owns claim
        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("You don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Default
    fun onUnclaim(player: Player) {
        onPartition(player)
    }

    @Subcommand("partition")
    fun onPartition(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!

        // Remove claim partition
        plugin.claimContainer.removePersistentClaimPartition(claimPartition)

        // Remove claim if there are no more partitions attached to it
        if (claimPartition.claim.claimPartitions.isEmpty()) {
            plugin.playerContainer.getPlayer(player.uniqueId)?.claims?.remove(claimPartition.claim)
            plugin.claimContainer.removePersistentClaim(claimPartition.claim)
        }

        player.sendMessage("This claim partition has been removed")
    }

    @Subcommand("connected")
    fun onConnected(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!

        // Remove claim and all partitions
        val claim = claimPartition.claim
        for (partition in claim.claimPartitions) {
            plugin.claimContainer.removePersistentClaimPartition(partition)
        }
        plugin.playerContainer.getPlayer(player.uniqueId)?.claims?.remove(claimPartition.claim)
        plugin.claimContainer.removePersistentClaim(claim)
        plugin.claimVisualiser.updateVisualisation(player, true)

        player.sendMessage("The entire claim has been removed.")
    }
}