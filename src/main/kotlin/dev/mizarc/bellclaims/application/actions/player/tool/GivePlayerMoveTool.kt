package dev.mizarc.bellclaims.application.actions.player.tool

import dev.mizarc.bellclaims.application.errors.PlayerNotFoundException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.player.tool.GivePlayerMoveToolResult
import dev.mizarc.bellclaims.application.services.ToolItemService
import java.util.UUID

class GivePlayerMoveTool(private val claimRepository: ClaimRepository, private val toolItemService: ToolItemService) {
    fun execute(playerId: UUID, claimId: UUID): GivePlayerMoveToolResult {
        val claim = claimRepository.getById(claimId) ?: return GivePlayerMoveToolResult.ClaimNotFound

        try {
            if (toolItemService.doesPlayerHaveMoveTool(playerId, claim))
                return GivePlayerMoveToolResult.PlayerAlreadyHasTool
        }
        catch (_: PlayerNotFoundException) {
            return GivePlayerMoveToolResult.PlayerNotFound
        }

        toolItemService.giveClaimTool(playerId)
        return GivePlayerMoveToolResult.Success
    }
}