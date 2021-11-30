package xyz.mizarc.solidclaims.claims

import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.*
import kotlin.collections.ArrayList

class Claim(var id: UUID, var world: World, var owner: OfflinePlayer, var players: ArrayList<Player>) {
    constructor(world: World, owner: OfflinePlayer) : this(UUID.randomUUID(), world, owner, ArrayList())
}