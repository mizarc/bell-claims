package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin

class SolidClaims : JavaPlugin() {
    internal var commandManager: PaperCommandManager = PaperCommandManager(this)

    override fun onEnable() {
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
