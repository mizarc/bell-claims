package dev.mizarc.bellclaims.application.actions.player.tool

import dev.mizarc.bellclaims.application.services.ToolItemService

class IsItemMoveTool(private val toolItemService: ToolItemService) {
    fun execute(itemData: Map<String, String>?): Boolean {
        return toolItemService.isMoveTool(itemData)
    }
}