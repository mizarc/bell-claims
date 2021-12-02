package xyz.mizarc.solidclaims.claims

import xyz.mizarc.solidclaims.events.ClaimPermission
import java.util.*
import kotlin.collections.ArrayList

/**
 * Contains a list of claim permissions for a particular player.
 * @property id The unique identifier for a player.
 * @property claimPermissions An array of claim permissions.
 */
class ClaimPlayer(var id: UUID, var claimPermissions: ArrayList<ClaimPermission>) {
    /**
     * Constructs a ClaimPlayer without a default list of permissions.
     * @param id The unique identifier for a player.
     */
    constructor(id: UUID) : this(id, ArrayList())
}