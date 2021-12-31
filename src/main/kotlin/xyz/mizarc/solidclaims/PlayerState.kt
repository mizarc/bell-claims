package xyz.mizarc.solidclaims

import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.PlayerAccess
import java.util.*
import kotlin.collections.ArrayList

class PlayerState(var id: UUID, var claimLimit: Int, var claimBlockLimit: Int,
                  var bonusClaims: Int, var bonusClaimBlocks: Int,
                  var claims: ArrayList<Claim>, var globalPermissions: ArrayList<PlayerAccess>) {
    var claimOverride = false

    constructor(id: UUID, claimLimit: Int, claimBlockLimit: Int) :
            this(id, claimLimit, claimBlockLimit, 0, 0, arrayListOf(), arrayListOf())

    fun getUsedClaimCount() : Int {
        var count = 0
        for (claim in claims) {
            count += 1
        }
        return count
    }

    fun getUsedClaimBlockCount() : Int {
        var count = 0
        for (claim in claims) {
            count += claim.getBlockCount()
        }
        return count
    }

    fun getTotalClaimLimit() : Int {
        return claimLimit + bonusClaims
    }

    fun getTotalClaimBlockLimit() : Int {
        return claimBlockLimit + bonusClaimBlocks
    }

    fun getRemainingClaimCount() : Int {
        return getTotalClaimLimit() - getUsedClaimCount()
    }

    fun getRemainingClaimBlockCount() : Int {
        return getTotalClaimBlockLimit() - getUsedClaimBlockCount()
    }
}