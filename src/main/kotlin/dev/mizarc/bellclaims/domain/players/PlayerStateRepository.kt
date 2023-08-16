package dev.mizarc.bellclaims.domain.players

import org.bukkit.OfflinePlayer

interface PlayerStateRepository {
    fun getAll() : Set<PlayerState>
    fun get(player: OfflinePlayer) : PlayerState?
    fun add(playerState: PlayerState)
    fun remove(playerState: PlayerState)
}