package dev.mizarc.bellclaims.application.actions.player.tool

import dev.mizarc.bellclaims.application.results.player.tool.GetClaimIdFromMoveToolResult
import dev.mizarc.bellclaims.application.services.ToolItemService
import java.util.UUID

class GetClaimIdFromMoveTool(private val toolItemService: ToolItemService) {
    fun execute(itemData: Map<String, String>?): GetClaimIdFromMoveToolResult {
        val claimIdString = toolItemService.getClaimIdFromPlayerMoveTool(itemData)
        if (claimIdString != null) {
            val claimId = UUID.fromString(claimIdString)
            return GetClaimIdFromMoveToolResult.Success(claimId)
        }
        return GetClaimIdFromMoveToolResult.NotMoveTool
    }
}