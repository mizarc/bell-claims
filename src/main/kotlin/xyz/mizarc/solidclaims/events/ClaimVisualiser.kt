package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.getClaimTool

class ClaimVisualiser(val plugin: SolidClaims) : Listener {
    var playerVisualisingState: MutableMap<Player, Boolean> = HashMap()

    companion object {
        // TODO: Cover more transparent materials
        private val transparentMaterials = arrayOf(
            Material.AIR,
            Material.WATER,
            Material.LAVA,
            Material.GLASS
        )
    }

    /**
     * Determine if [player] should be seeing the borders of nearby claims, and if so, send fake block data representing those bounds.
     */
    private fun updateVisualisation(player: Player) {
        val holdingClaimTool = player.inventory.itemInMainHand == getClaimTool() || player.inventory.itemInOffHand == getClaimTool()
        val state = playerVisualisingState[player] ?: !holdingClaimTool
        if (state == holdingClaimTool) return
        playerVisualisingState[player] = holdingClaimTool

        val chunks = getSurroundingChunks(ClaimContainer.getChunkLocation(ClaimContainer.getPositionFromLocation(player.location)), plugin.server.viewDistance)
        val partitions = getClaimPartitionsInChunks(chunks)
        if (partitions.isEmpty()) return

        val borders: ArrayList<Pair<Int, Int>> = ArrayList()
        for (part in partitions) {
            borders.addAll(part.getEdgeBlockPositions())
        }

        for (block in borders) {
            for (y in player.location.blockY-25..player.location.blockY+25) { // Get all blocks on claim borders within 25 blocks up and down from the player's current position
                var blockData = Material.CYAN_GLAZED_TERRACOTTA.createBlockData() // Set the visualisation block
                val blockLocation = Location(player.location.world, block.first.toDouble(), y.toDouble(), block.second.toDouble()) // Get the location of the block being considered currently
                if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue // If the block is transparent, skip it
                if (!isBlockVisible(blockLocation)) continue // If the block isn't considered to be visible, skip it
                if (!playerVisualisingState[player]!!) blockData = player.world.getBlockAt(blockLocation).blockData // If visualisation is being disabled, get the real block data
                player.sendBlockChange(blockLocation, blockData) // Send the player block updates
            }
        }
    }

    /**
     * Determine if a block is a floor/ceiling and therefore should be considered visible
     */
    private fun isBlockVisible(loc: Location): Boolean {
        val above = Location(loc.world, loc.x, loc.y+1, loc.z).block.blockData.material
        val below = Location(loc.world, loc.x, loc.y-1, loc.z).block.blockData.material
        return transparentMaterials.contains(above) || transparentMaterials.contains(below)
    }

    @EventHandler
    fun onHoldClaimTool(event: PlayerItemHeldEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { updateVisualisation(event.player) }) // Run task after this tick
    }

    @EventHandler
    fun onPickupClaimTool(event: EntityPickupItemEvent) {
        if (event.entityType != EntityType.PLAYER) return
        plugin.server.scheduler.runTask(plugin, Runnable { updateVisualisation(event.entity as Player) }) // Run task after this tick
    }

    @EventHandler
    fun onDropClaimTool(event: PlayerDropItemEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { updateVisualisation(event.player) }) // Run task after this tick
    }

    @EventHandler
    fun onClaimToolInventoryInteract(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        plugin.server.scheduler.runTask(plugin, Runnable { updateVisualisation(player) }) // Run task after this tick
    }

    /**
     * Determine what claim partitions are within [chunks]
     */
    private fun getClaimPartitionsInChunks(chunks: Array<Pair<Int, Int>>): Array<ClaimPartition> {
        val claims: ArrayList<ClaimPartition> = ArrayList()

        for (chunk in chunks) {
            val parts = plugin.claimContainer.getClaimPartitionsAtChunk(chunk) ?: continue
            for (part in parts) {
                if (claims.contains(part)) continue
                claims.add(part)
            }
        }
        return claims.toTypedArray()
    }

    /**
     * Get a square of chunks centering on [loc] with a size of [radius]
     */
    @Suppress("SameParameterValue")
    private fun getSurroundingChunks(loc: Pair<Int, Int>, radius: Int): Array<Pair<Int, Int>> {
        val sideLength = (radius * 2) + 1 // Make it always odd (eg. radius of 2 results in 5x5 square)
        val chunks: Array<Pair<Int, Int>> = Array(sideLength * sideLength) {Pair(0, 0)}

        for (x in 0 until sideLength) {
            for (z in 0 until sideLength) {
                chunks[(x * sideLength) + z] = Pair(loc.first + x - radius, loc.second + z - radius)
            }
        }

        return chunks
    }
}