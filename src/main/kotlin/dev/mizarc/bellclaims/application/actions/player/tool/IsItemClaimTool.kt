package dev.mizarc.bellclaims.application.actions.player.tool

import dev.mizarc.bellclaims.application.services.ToolItemService

class IsItemClaimTool(private val toolItemService: ToolItemService) {
    fun execute(itemData: Map<String, String>?): Boolean {
        return toolItemService.isClaimTool(itemData)
    }
}