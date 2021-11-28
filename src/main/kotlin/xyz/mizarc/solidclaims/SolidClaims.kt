package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin

class SolidClaims : JavaPlugin() {
    internal var commandManager: PaperCommandManager = PaperCommandManager(this)

    companion object {
        internal lateinit var instance: SolidClaims
    }

    init {
        instance = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(ClaimEventHandler(), this)
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
