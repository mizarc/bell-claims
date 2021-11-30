package xyz.mizarc.solidclaims.claims

import org.bukkit.Location

class ClaimPartition(var claim: Claim, var firstPosition: Location, var secondPosition: Location) {
    fun isLocationInClaim(location: Location) : Boolean {
        if (location.world?.uid != claim.worldId) {
            return false
        }

        if (location.x >= firstPosition.x
            && location.x <= secondPosition.x
            && location.z >= firstPosition.z
            && location.z <= secondPosition.z) {
            return true
        }

        return false
    }

    fun getEdgeBlockPositions() : MutableSet<Pair<Int, Int>> {
        var blocks : MutableSet<Pair<Int, Int>> = mutableSetOf()
        for (block in firstPosition.x.toInt()..secondPosition.x.toInt()) {
            blocks.add(Pair(block, firstPosition.z.toInt()))
        }
        for (block in firstPosition.x.toInt()..secondPosition.x.toInt()) {
            blocks.add(Pair(block, secondPosition.z.toInt()))
        }
        for (block in firstPosition.z.toInt()..secondPosition.z.toInt()) {
            blocks.add(Pair(block, firstPosition.x.toInt()))
        }
        for (block in firstPosition.z.toInt()..secondPosition.z.toInt()) {
            blocks.add(Pair(block, secondPosition.x.toInt()))
        }
        return blocks
    }
}