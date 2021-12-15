package xyz.mizarc.solidclaims

import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.PlayerAccess
import java.util.*
import kotlin.collections.ArrayList

class PlayerState(var id: UUID, var claimLimit: Int, var claimBlockLimit: Int,
                  var bonusClaims: Int, var bonusClaimBlocks: Int,
                  var claims: ArrayList<Claim>, var globalPermissions: ArrayList<PlayerAccess>) {

    constructor(id: UUID, claimLimit: Int, claimBlockLimit: Int) :
            this(id, claimLimit, claimBlockLimit, 0, 0, arrayListOf(), arrayListOf())
}