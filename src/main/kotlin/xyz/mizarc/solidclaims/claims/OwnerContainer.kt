package xyz.mizarc.solidclaims.claims

import java.util.*
import kotlin.collections.ArrayList

/**
 * Holds a collection of every claim owner on the server.
 */
class OwnerContainer {
    lateinit var claimOwners: ArrayList<ClaimOwner>

    fun getOwner(playerId: UUID) : ClaimOwner? {
        for (claimOwner in claimOwners) {
            if (claimOwner.ownerId == playerId) {
                return claimOwner
            }
        }

        return null
    }
}