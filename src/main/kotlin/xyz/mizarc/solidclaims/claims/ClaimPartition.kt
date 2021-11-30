package xyz.mizarc.solidclaims.claims

import org.bukkit.Location

class ClaimPartition(var claim: Claim, var firstPosition: Pair<Int, Int>, var secondPosition: Pair<Int, Int>) {
    fun isLocationInClaim(location: Location) : Boolean {
        if (location.world?.uid != claim.worldId) {
            return false
        }

        if (location.x >= firstPosition.first
            && location.x <= secondPosition.first
            && location.z >= firstPosition.second
            && location.z <= secondPosition.second) {
            return true
        }

        return false
    }

    fun getEdgeBlockPositions() : MutableSet<Pair<Int, Int>> {
        var blocks : MutableSet<Pair<Int, Int>> = mutableSetOf()
        for (block in firstPosition.first..secondPosition.first) {
            blocks.add(Pair(block, firstPosition.second.toInt()))
        }
        for (block in firstPosition.first..secondPosition.first) {
            blocks.add(Pair(block, secondPosition.second))
        }
        for (block in firstPosition.second..secondPosition.second) {
            blocks.add(Pair(block, firstPosition.first))
        }
        for (block in firstPosition.second..secondPosition.second) {
            blocks.add(Pair(block, secondPosition.first))
        }
        return blocks
    }

    fun getFirstLocation() : Location {
        return Location(claim.getWorld(), firstPosition.first.toDouble(), 0.0, firstPosition.second.toDouble())
    }

    fun getSecondLocation() : Location {
        return Location(claim.getWorld(), secondPosition.first.toDouble(), 0.0, secondPosition.second.toDouble())
    }
}