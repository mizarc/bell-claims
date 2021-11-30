package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.commands.ClaimCommand
import xyz.mizarc.solidclaims.commands.HandleEventsCommand
import xyz.mizarc.solidclaims.events.ClaimEventHandler

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager
    internal var configIO: ConfigIO = ConfigIO(this)

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
        commandManager.registerCommand(HandleEventsCommand())
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
