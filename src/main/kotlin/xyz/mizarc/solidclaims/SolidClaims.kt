package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.commands.ClaimCommand

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager
    internal var configIO: ConfigIO = ConfigIO(this)

    override fun onEnable() {
        logger.info("SolidClaims has been Enabled")
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimCommand())
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
