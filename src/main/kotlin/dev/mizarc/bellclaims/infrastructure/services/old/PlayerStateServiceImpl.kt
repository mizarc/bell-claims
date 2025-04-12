package dev.mizarc.bellclaims.infrastructure.services.old

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.old.PlayerRegisterResult
import dev.mizarc.bellclaims.application.results.old.PlayerUnregisterResult
import dev.mizarc.bellclaims.application.services.old.PlayerStateService
import dev.mizarc.bellclaims.domain.entities.PlayerState
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

class PlayerStateServiceImpl(private val playerStateRepo: PlayerStateRepository): PlayerStateService {
    override fun isPlayerRegistered(player: OfflinePlayer): Boolean {
        return playerStateRepo.getAll().any { it.player.uniqueId == player.uniqueId }
    }

    override fun getAllOnline(): Set<PlayerState> {
        return playerStateRepo.getAll()
    }

    override fun getById(id: UUID): PlayerState? {
        return playerStateRepo.get(id)
    }

    override fun getByPlayer(player: OfflinePlayer): PlayerState? {
        return playerStateRepo.get(player.uniqueId)
    }

    override fun registerPlayer(player: Player): PlayerRegisterResult {
        if (isPlayerRegistered(player)) return PlayerRegisterResult.UNCHANGED
        playerStateRepo.add(PlayerState(player))
        return PlayerRegisterResult.SUCCESS
    }

    override fun unregisterPlayer(player: OfflinePlayer): PlayerUnregisterResult {
        val playerState = getById(player.uniqueId) ?: return PlayerUnregisterResult.UNCHANGED
        playerStateRepo.remove(playerState)
        return PlayerUnregisterResult.SUCCESS
    }
}