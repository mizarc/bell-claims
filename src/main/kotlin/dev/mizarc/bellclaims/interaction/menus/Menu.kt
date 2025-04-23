package dev.mizarc.bellclaims.interaction.menus

interface Menu {
    fun open()
    fun passData(data: Any?) {
        // Default implementation does nothing
    }
}