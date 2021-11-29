package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.commands.ClaimCommand

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager

    companion object {
        internal lateinit var instance: SolidClaims
    }

    init {
        instance = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(ClaimEventHandler(), this)
        logger.info("SolidClaims has been Enabled")
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimCommand())
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
