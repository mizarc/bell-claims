package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

class PlayerStateServiceImpl(private val playerStateRepo: PlayerStateRepository): PlayerStateService {
    override fun isPlayerRegistered(player: OfflinePlayer): Boolean {
        return playerStateRepo.getAll().any { it.player == player }
    }

    override fun getAllOnline(): Set<PlayerState> {
        return playerStateRepo.getAll()
    }

    override fun getById(id: UUID): PlayerState? {
        val player = Bukkit.getOfflinePlayer(id)
        return playerStateRepo.get(player)
    }

    override fun getByPlayer(player: OfflinePlayer): PlayerState? {
        return playerStateRepo.get(player)
    }

    override fun registerPlayer(player: Player) {
        playerStateRepo.add(PlayerState(player))
    }

    override fun unregisterPlayer(player: OfflinePlayer) {
        val playerState = getById(player.uniqueId) ?: return
        playerStateRepo.remove(playerState)
    }
}