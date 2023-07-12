package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.ClaimQuery
import xyz.mizarc.solidclaims.claims.ClaimRepository
import xyz.mizarc.solidclaims.listeners.ClaimVisualiser
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.players.PlayerStateRepository


@CommandAlias("unclaim")
class UnclaimCommand : BaseCommand() {
    @Dependency
    lateinit var claims : ClaimRepository
    lateinit var partitions: PartitionRepository
    lateinit var playerStates: PlayerStateRepository
    lateinit var claimVisualiser: ClaimVisualiser
    protected lateinit var claimQuery: ClaimQuery

    @Default
    @CommandPermission("solidclaims.command.unclaim")
    fun onUnclaim(player: Player) {
        onPartition(player)
    }

    @Subcommand("partition")
    @CommandPermission("solidclaims.command.unclaim.partition")
    fun onPartition(player: Player) {
        val partition = claimQuery.getByPlayer(player) ?: return

        // Remove claim and send alert if not executed
        if (!claimQuery.removePartition(partition)) {
            partitions.add(partition)
            return player.sendMessage("§cThat resize would result in an unconnected partition island.")
        }

        // Update visualiser
        claimVisualiser.oldPartitions.add(partition)
        claimVisualiser.unrenderOldClaims(player)
        claimVisualiser.oldPartitions.clear()

        // Remove claim if there are no more partitions attached to it
        if (partitions.getByClaim(partition.claimId).isEmpty()) {
            val claim = claims.getById(partition.claimId) ?: return
            claims.remove(claim)
            player.sendMessage("§aThe claim has been removed.")
            return
        }

        player.sendMessage("§aThis claim partition has been removed")
    }

    @Subcommand("connected")
    @CommandPermission("solidclaims.command.unclaim.connected")
    fun onConnected(player: Player) {
        val partition = claimQuery.getByPlayer(player) ?: return
        val claim = claims.getById(partition.claimId) ?: return
        val claimPartitions = partitions.getByClaim(partition.claimId)

        for (claimPartition in claimPartitions) {
            partitions.remove(partition)
        }

        claims.remove(claim)
        claimVisualiser.oldPartitions.addAll(claimPartitions)
        claimVisualiser.unrenderOldClaims(player)
        claimVisualiser.oldPartitions.clear()
        claimVisualiser.updateVisualisation(player, true)

        player.sendMessage("§aThe claim has been removed.")
    }
}