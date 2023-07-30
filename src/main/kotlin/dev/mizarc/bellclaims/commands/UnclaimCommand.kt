package dev.mizarc.bellclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.ClaimService
import dev.mizarc.bellclaims.PartitionService
import dev.mizarc.bellclaims.claims.ClaimRepository
import dev.mizarc.bellclaims.listeners.ClaimVisualiser
import dev.mizarc.bellclaims.partitions.PartitionRepository
import dev.mizarc.bellclaims.players.PlayerStateRepository


@CommandAlias("unclaim")
class UnclaimCommand : BaseCommand() {
    @Dependency
    lateinit var claims : ClaimRepository
    lateinit var partitions: PartitionRepository
    lateinit var playerStates: PlayerStateRepository
    lateinit var claimVisualiser: ClaimVisualiser
    protected lateinit var claimService: ClaimService
    protected lateinit var partitionService: PartitionService

    @Default
    @CommandPermission("bellclaims.command.unclaim")
    fun onUnclaim(player: Player) {
        onPartition(player)
    }

    @Subcommand("partition")
    @CommandPermission("bellclaims.command.claim.partition")
    fun onPartition(player: Player) {
        val partition = partitionService.getByPlayerPosition(player) ?: return

        // Remove claim and send alert if not executed
        if (!partitionService.removePartition(partition)) {
            partitions.add(partition)
            return player.sendMessage("§cThat resize would result in an unconnected partition island.")
        }

        // Update visualiser
        val claim = claims.getById(partition.claimId) ?: return
        claimVisualiser.registerClaimUpdate(claim)

        // Remove claim if there are no more partitions attached to it
        if (partitions.getByClaim(claim).isEmpty()) {
            claims.remove(claim)
            player.sendMessage("§aThe claim has been removed.")
            return
        }

        player.sendMessage("§aThis claim partition has been removed")
    }
}