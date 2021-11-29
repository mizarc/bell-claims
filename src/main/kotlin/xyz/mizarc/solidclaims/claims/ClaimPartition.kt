package xyz.mizarc.solidclaims.claims

import org.bukkit.Location

class ClaimPartition(var claim: Claim, var firstPosition: Location, var secondPosition: Location) {
    fun isLocationInClaim(location: Location) : Boolean {
        if (location.world?.name != claim.world) {
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
}