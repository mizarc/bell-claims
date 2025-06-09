package dev.mizarc.bellclaims.application.actions.player.tool

import dev.mizarc.bellclaims.application.errors.PlayerNotFoundException
import dev.mizarc.bellclaims.application.results.player.tool.GivePlayerClaimToolResult
import dev.mizarc.bellclaims.application.services.ToolItemService
import java.util.UUID

class GivePlayerClaimTool(private val toolItemService: ToolItemService) {
    fun execute(playerId: UUID): GivePlayerClaimToolResult {
        try {
            if (toolItemService.doesPlayerHaveClaimTool(playerId)) return GivePlayerClaimToolResult.PlayerAlreadyHasTool
        }
        catch (_: PlayerNotFoundException) {
            return GivePlayerClaimToolResult.PlayerNotFound
        }

        toolItemService.giveClaimTool(playerId)
        return GivePlayerClaimToolResult.Success
    }
}