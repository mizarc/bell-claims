package xyz.mizarc.solidclaims.claims

import xyz.mizarc.solidclaims.events.ClaimPermission
import java.util.*
import kotlin.collections.ArrayList

/**
 * Contains the global and player level permissions for a particular claim owner. Permissions are linked to the player
 * rather than to any particular claim.
 * @param ownerId The unique identifier for the player.
 * @param globalPermissions The default permissions of claims.
 * @param playerPermissions The permissions assigned to a particular player.
 */
class ClaimOwner(var ownerId: UUID, var globalPermissions: ArrayList<ClaimPermission>,
                 var playerPermissions: ArrayList<PlayerAccess>) {
    /**
     * Constructs a ClaimOwner without any permissions.
     * @param ownerId The unique identifier for the player.
     */
    constructor(ownerId: UUID) : this(ownerId, ArrayList(), ArrayList())
}