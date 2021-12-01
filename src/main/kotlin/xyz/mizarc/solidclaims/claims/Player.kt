package xyz.mizarc.solidclaims.claims

import xyz.mizarc.solidclaims.events.ClaimPermission
import java.util.*
import kotlin.collections.ArrayList

class Player(var id: UUID, var claimPermissions: ArrayList<ClaimPermission>) {
    constructor(id: UUID) : this(id, ArrayList())
}