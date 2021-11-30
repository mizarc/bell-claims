package xyz.mizarc.solidclaims.claims

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.*
import kotlin.collections.ArrayList

class Claim(var id: UUID, var worldId: UUID, var owner: OfflinePlayer, var players: ArrayList<Player>) {
    constructor(worldId: UUID, owner: OfflinePlayer) : this(UUID.randomUUID(), worldId, owner, ArrayList())

    fun getWorld() : World? {
        return Bukkit.getWorld(worldId)
    }
}