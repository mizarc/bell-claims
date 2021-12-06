package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.getClaimTool

class ClaimVisualiser(val plugin: SolidClaims) : Listener {
    var playerVisualisingState: MutableMap<Player, Boolean> = HashMap()

    @EventHandler
    fun onHoldClaimTool(event: PlayerItemHeldEvent) {
        val holdingClaimTool = event.player.inventory.itemInMainHand == getClaimTool() || event.player.inventory.itemInOffHand == getClaimTool()
        val state: Boolean = playerVisualisingState[event.player] ?: !holdingClaimTool // If map does not contain player, set value to opposite of holdingClaimTool to force it to update
        if (state == holdingClaimTool) return

        playerVisualisingState[event.player] = holdingClaimTool // Register state to the cache

        val chunks = getSurroundingChunks(ClaimContainer.getChunkLocation(ClaimContainer.getPositionFromLocation(event.player.location)), 2)
        val partitions = getClaimPartitionsInChunks(chunks)
        if (partitions.isEmpty()) return

        val borders: ArrayList<Pair<Int, Int>> = ArrayList()
        for (part in partitions) {
            borders.addAll(part.getEdgeBlockPositions())
        }
        // TODO: Make this not flood packets please
        for (block in borders) {
            for (y in -64..255) {
                var blockData = Material.CYAN_GLAZED_TERRACOTTA.createBlockData() // Set the visualisation block
                val blockLocation = Location(event.player.location.world, block.first.toDouble(), y.toDouble(), block.second.toDouble())
                if (blockLocation.block.blockData.material == Material.AIR) continue
                if (!playerVisualisingState[event.player]!!) blockData = event.player.world.getBlockAt(blockLocation).blockData // If visualisation is being disabled, get the real block data
                event.player.sendBlockChange(blockLocation, blockData)
            }
        }
    }

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

    @Suppress("SameParameterValue")
    private fun getSurroundingChunks(loc: Pair<Int, Int>, radius: Int): Array<Pair<Int, Int>> {
        val sideLength = (radius * 2) + 1 // Make it always odd (eg. radius of 2 results in 5x5 square
        val chunks: Array<Pair<Int, Int>> = Array(sideLength * sideLength) {Pair(0, 0)}

        for (x in 0 until sideLength) {
            for (z in 0 until sideLength) {
                chunks[(x * sideLength) + z] = Pair(loc.first + x - radius, loc.second + z - radius)
            }
        }

        return chunks
    }
}