package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.Partition


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

        // Check if player state exists
        val playerState = plugin.playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("Somehow, your player data doesn't exist. Please contact an administrator.")
            return true
        }

        if (playerState.claimOverride) {
            return false
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

        // Check if claim resize would result in any claim islands
        plugin.claimContainer.removeClaimPartition(claimPartition)
        if (claimPartition.claim.isAnyDisconnectedPartitions()) {
            plugin.claimContainer.addClaimPartition(claimPartition)
            return player.sendMessage(
                "That resize would result in an unconnected partition island."
            )
        }

        // Remove claim partition
        plugin.claimContainer.addClaimPartition(claimPartition)
        plugin.claimContainer.removePersistentClaimPartition(claimPartition)
        plugin.claimVisualiser.oldPartitions.add(claimPartition)
        plugin.claimVisualiser.unrenderOldClaims(player)
        plugin.claimVisualiser.oldPartitions.clear()


        // Remove claim if there are no more partitions attached to it
        if (claimPartition.claim.partitions.isEmpty()) {
            plugin.playerContainer.getPlayer(player.uniqueId)?.claims?.remove(claimPartition.claim)
            plugin.claimContainer.removePersistentClaim(claimPartition.claim)
            player.sendMessage("The claim has been removed.")
            return
        }

        player.sendMessage("This claim partition has been removed")
    }

    @Subcommand("connected")
    fun onConnected(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!

        // Remove claim and all partitions
        val claim = claimPartition.claim
        val partitionsToRemove = ArrayList<Partition>()
        for (partition in claim.partitions) {
            partitionsToRemove.add(partition)

        }
        for (partition in partitionsToRemove) {
            plugin.claimContainer.removePersistentClaimPartition(partition)
        }

        plugin.playerContainer.getPlayer(player.uniqueId)?.claims?.remove(claimPartition.claim)
        plugin.claimContainer.removePersistentClaim(claim)
        plugin.claimVisualiser.oldPartitions.addAll(partitionsToRemove)
        plugin.claimVisualiser.unrenderOldClaims(player)
        plugin.claimVisualiser.oldPartitions.clear()
        plugin.claimVisualiser.updateVisualisation(player, true)

        player.sendMessage("The claim has been removed.")
    }
}