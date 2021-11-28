package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.commands.Claim

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager

    override fun onEnable() {
        logger.info("SolidClaims has been Enabled")
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(Claim())
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
