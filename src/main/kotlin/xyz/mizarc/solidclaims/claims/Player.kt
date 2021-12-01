package xyz.mizarc.solidclaims.claims

import java.util.*
import kotlin.collections.ArrayList

class Player(var id: UUID, var permissions: ArrayList<Permission>) {
    constructor(id: UUID) : this(id, ArrayList())
}