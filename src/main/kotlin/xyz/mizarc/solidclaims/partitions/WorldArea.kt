package xyz.mizarc.solidclaims.partitions

import java.util.*

class WorldArea(override var lowerPosition: Position, override var upperPosition: Position, val worldId: UUID): Area(lowerPosition, upperPosition) {
    constructor(area: Area, worldId: UUID): this(area.lowerPosition, area.upperPosition, worldId)

    fun getWorldChunks(): ArrayList<WorldPosition> {
        val firstChunk = lowerPosition.toChunk()
        val secondChunk = upperPosition.toChunk()

        val chunks: ArrayList<WorldPosition> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(WorldPosition(x, z, worldId))
            }
        }

        return chunks
    }
}