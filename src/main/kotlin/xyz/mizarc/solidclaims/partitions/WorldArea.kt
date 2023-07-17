package xyz.mizarc.solidclaims.partitions

import java.util.*

class WorldArea(lowerPosition2D: Position2D, upperPosition2D: Position2D, val worldId: UUID): Area(lowerPosition2D, upperPosition2D) {

    constructor(area: Area, worldId: UUID): this(area.lowerPosition2D, area.upperPosition2D, worldId)

    fun getWorldChunks(): ArrayList<Position2D> {
        val firstChunk = lowerPosition2D.toChunk()
        val secondChunk = upperPosition2D.toChunk()

        val chunks: ArrayList<Position2D> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(Position2D(x, z))
            }
        }

        return chunks
    }
}