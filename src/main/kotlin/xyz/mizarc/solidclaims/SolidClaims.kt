package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.OwnerContainer
import xyz.mizarc.solidclaims.commands.ClaimCommand
import xyz.mizarc.solidclaims.commands.HandleEventsCommand
import xyz.mizarc.solidclaims.events.ClaimEventHandler
import xyz.mizarc.solidclaims.events.ClaimToolListener

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager
    internal var configIO: ConfigIO = ConfigIO(this)
    lateinit var claimContainer: ClaimContainer
    lateinit var ownerContainer: OwnerContainer


    override fun onEnable() {
        logger.info("SolidClaims has been Enabled")
        claimContainer = ClaimContainer()
        ownerContainer = OwnerContainer()
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimContainer), this)
        server.pluginManager.registerEvents(ClaimToolListener(), this)
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(HandleEventsCommand())
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
