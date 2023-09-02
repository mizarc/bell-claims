package dev.mizarc.bellclaims.domain.players

import org.bukkit.OfflinePlayer

interface PlayerStateRepository {
    fun getAll() : Set<PlayerState>

    /**
     * Gets the player state for a specific player.
     * @param player The player to fetch.
     * @return A PlayerState object of the player. May return null.
     */
    fun get(player: OfflinePlayer) : PlayerState?
    fun add(playerState: PlayerState)
    fun remove(playerState: PlayerState)
}