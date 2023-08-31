package dev.mizarc.bellclaims.interaction.visualisation

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.VisualisationService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.utils.carpetBlocks
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.concurrent.thread

class Visualiser(private val plugin: JavaPlugin,
                 private val claimService: ClaimService,
                 private val partitionService: PartitionService,
                 private val playerStateRepo: PlayerStateRepository,
                 private val visualisationService: VisualisationService) : Listener {
    /**
     * Display claim visualisation to target player
     */
    fun show(player: Player): MutableMap<Claim, Set<Position3D>> {
        val playerState = playerStateRepo.get(player) ?: return mutableMapOf()

        // Change visualiser depending on view mode
        val borders: MutableMap<Claim, Set<Position3D>> = mutableMapOf()
        borders.putAll(updateOthersVisualisation(player))
        if (playerState.claimToolMode == 1) {
            borders.putAll(updatePartitionVisualisation(player))
        }
        else {
            borders.putAll(updateClaimVisualisation(player))
        }

        // Set visualisation in player state
        playerState.visualisedBlockPositions = borders
        playerState.isVisualisingClaims = true
        return borders
    }

    /**
     * Display claim visualisation to target player
     */
    fun show(player: Player, claim: Claim): Set<Position3D> {
        val playerState = playerStateRepo.get(player) ?: return mutableSetOf()

        // Change visualiser depending on view mode
        val borders: Set<Position3D> = when {
            claim.owner.uniqueId != player.uniqueId -> updateOthersVisualisation(player, claim)
            playerState.claimToolMode == 1 -> updatePartitionVisualisation(player, claim)
            else -> updateClaimVisualisation(player, claim)
        }

        // Set visualisation in player state
        playerState.visualisedBlockPositions[claim] = borders
        playerState.isVisualisingClaims = true
        return borders
    }

    /**
     * Hide claim visualisation for target player
     */
    fun hide(player: Player) {
        val playerState = playerStateRepo.get(player) ?: return
        revertVisualisedBlocks(player)
        playerState.visualisedBlockPositions.clear()
        playerState.isVisualisingClaims = false
    }

    /**
     * Load a new visualiser display for a target player who is already visualising
     */
    fun refresh(player: Player) {
        val playerState = playerStateRepo.get(player) ?: return

        // Get all currently visualised blocks
        val currentVisualised = playerState.visualisedBlockPositions.values.flatten().toMutableSet()

        val borders = show(player).values.flatten().toMutableSet()
        currentVisualised.removeAll(borders.toSet())
        revertVisualisedBlocks(player, currentVisualised)
    }

    /**
     * Visualise a player's claims with individual partitions shown.
     */
    fun updatePartitionVisualisation(player: Player): Map<Claim, Set<Position3D>> {
        val chunks = getSurroundingChunks(player.chunk, plugin.server.viewDistance)
        val partitions = chunks.flatMap { partitionService.getByChunk(it) }
        if (partitions.isEmpty()) return mutableMapOf()

        val visualised: MutableMap<Claim, Set<Position3D>> = mutableMapOf()
        for (partition in partitions) {
            val claim = claimService.getById(partition.claimId) ?: continue
            if (visualised.containsKey(claim)) continue
            if (claim.owner.uniqueId != player.uniqueId) continue

            visualised[claim] = updatePartitionVisualisation(player, claim)
        }
        return visualised
    }

    /**
     * Visualise a player's specific claim with individual partitions shown.
     */
    fun updatePartitionVisualisation(player: Player, claim: Claim): Set<Position3D> {
        val mainBorders = visualisationService.get3DMainPartitionBorders(claim, player.location)
        val mainCorners = visualisationService.get3DMainPartitionCorners(claim, player.location)
        val borders = visualisationService.get3DPartitionedBorders(claim, player.location).values.flatten().toSet()
        val corners = visualisationService.get3DPartitionedCorners(claim, player.location).values.flatten().toSet()

        setVisualisedBlocks(player, mainBorders, Material.CYAN_GLAZED_TERRACOTTA, Material.CYAN_CARPET)
        setVisualisedBlocks(player, mainCorners, Material.BLUE_GLAZED_TERRACOTTA, Material.BLUE_CARPET)
        setVisualisedBlocks(player, borders, Material.LIGHT_GRAY_GLAZED_TERRACOTTA, Material.LIGHT_GRAY_CARPET)
        setVisualisedBlocks(player, corners, Material.LIGHT_BLUE_GLAZED_TERRACOTTA, Material.LIGHT_BLUE_CARPET)
        return (mainBorders + mainCorners + borders + corners).toSet()
    }

    /**
     * Visualise all of a player's claims with only outer borders.
     */
    fun updateClaimVisualisation(player: Player): Map<Claim, Set<Position3D>> {
        val chunks = getSurroundingChunks(player.chunk, plugin.server.viewDistance)
        val partitions = chunks.flatMap { partitionService.getByChunk(it) }
        if (partitions.isEmpty()) return mutableMapOf()

        val visualised: MutableMap<Claim, Set<Position3D>> = mutableMapOf()
        for (partition in partitions) {
            val claim = claimService.getById(partition.claimId) ?: continue
            if (visualised.containsKey(claim)) continue
            if (claim.owner.uniqueId != player.uniqueId) continue

            visualised[claim] = updateClaimVisualisation(player, claim)
        }
        return visualised
    }

    /**
     * Visualise a player's specific claim with only outer borders.
     */
    fun updateClaimVisualisation(player: Player, claim: Claim): Set<Position3D> {
        val borders = visualisationService.get3DOuterBorders(claim, player.location)
        setVisualisedBlocks(player, borders, Material.LIGHT_BLUE_GLAZED_TERRACOTTA, Material.LIGHT_GRAY_CARPET)
        return borders
    }

    /**
     * Visualise claims that aren't owned by the player.
     */
    fun updateOthersVisualisation(player: Player): Map<Claim, Set<Position3D>> {
        val chunks = getSurroundingChunks(player.chunk, plugin.server.viewDistance)
        val partitions = chunks.flatMap { partitionService.getByChunk(it) }
        if (partitions.isEmpty()) return mutableMapOf()

        val visualised: MutableMap<Claim, Set<Position3D>> = mutableMapOf()
        for (partition in partitions) {
            val claim = claimService.getById(partition.claimId) ?: continue
            if (visualised.containsKey(claim)) continue
            if (claim.owner.uniqueId == player.uniqueId) continue

            visualised[claim] = updateOthersVisualisation(player, claim)
        }
        return visualised
    }

    /**
     * Visualise a specific claim that isn't owned by a player.
     */
    fun updateOthersVisualisation(player: Player, claim: Claim): Set<Position3D> {
        val borders = visualisationService.get3DOuterBorders(claim, player.location)
        setVisualisedBlocks(player, borders, Material.RED_GLAZED_TERRACOTTA, Material.LIGHT_GRAY_CARPET)
        return borders
    }

    /**
     * Get a square of chunks centering on [chunk] with a size of [radius]
     */
    fun getSurroundingChunks(chunk: Chunk, radius: Int): Set<Chunk> {
        val sideLength = (radius * 2) + 1 // Make it always odd (e.g. radius of 2 results in 5x5 square)
        val chunks: MutableSet<Chunk> = mutableSetOf()

        for (x in 0 until sideLength) {
            for (z in 0 until sideLength) {
                chunks.add(chunk.world.getChunkAt(chunk.x + x - radius, chunk.z + z - radius))
            }
        }

        return chunks
    }

    fun setVisualisedBlocks(player: Player, positions: Set<Position3D>, block: Material, flatBlock: Material) {
        positions.forEach { setVisualisedBlock(player, it, block, flatBlock) }
    }

    fun setVisualisedBlock(player: Player, position: Position3D, block: Material, flatBlock: Material) {
        val blockLocation = Location(player.location.world, position.x.toDouble(),
            position.y.toDouble(), position.z.toDouble())
        val blockData = if (carpetBlocks.contains(blockLocation.block.blockData.material))
            flatBlock.createBlockData() else block.createBlockData()
        player.sendBlockChange(blockLocation, blockData)
    }

    fun revertVisualisedBlocks(player: Player, positions: Set<Position3D>) {
        val playerState = playerStateRepo.get(player) ?: return
        val removed = mutableSetOf<Position3D>()
        for (position in positions) {
            val blockData = player.world.getBlockAt(position.toLocation(player.world)).blockData
            player.sendBlockChange(position.toLocation(player.world), blockData)
            removed.add(position)
        }

        for (visualising in playerState.visualisedBlockPositions) {
            val visualisingTemp = visualising.value.toMutableSet()
            visualisingTemp.removeAll(removed)
            playerState.visualisedBlockPositions[visualising.key] = visualisingTemp
        }
    }

    fun revertVisualisedBlocks(player: Player) {
        val playerState = playerStateRepo.get(player) ?: return
        for (claim in playerState.visualisedBlockPositions) {
            for (position in claim.value) {
                val blockData = player.world.getBlockAt(position.toLocation(player.world)).blockData
                player.sendBlockChange(position.toLocation(player.world), blockData)
            }
        }
    }
}