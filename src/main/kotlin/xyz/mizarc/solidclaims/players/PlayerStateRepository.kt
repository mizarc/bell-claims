package xyz.mizarc.solidclaims.players

import co.aikar.idb.Database
import net.milkbowl.vault.chat.Chat
import org.bukkit.OfflinePlayer
import xyz.mizarc.solidclaims.Config
import xyz.mizarc.solidclaims.storage.Storage
import java.util.*
import kotlin.collections.ArrayList

/**
 * Holds a collection of every player on the server.
 */
class PlayerStateRepository {
    var playerStates: MutableMap<UUID, PlayerState> = mutableMapOf()

    /**
     * Gets a specific player state.
     * @param player The player to fetch.
     * @return A PlayerState object of the player. May return null.
     */
    fun get(player: OfflinePlayer) : PlayerState? {
        return playerStates[player.uniqueId]
    }

    fun add(playerState: PlayerState) {
        playerStates[playerState.player.uniqueId] = playerState
    }

    fun removePlayer(playerState: PlayerState){
        playerStates.remove(playerState.player.uniqueId)
    }
}