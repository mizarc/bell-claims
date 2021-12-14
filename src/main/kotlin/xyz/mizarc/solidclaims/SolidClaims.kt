package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.OwnerContainer
import xyz.mizarc.solidclaims.commands.*
import xyz.mizarc.solidclaims.events.ClaimEventHandler
import xyz.mizarc.solidclaims.events.ClaimPermission
import xyz.mizarc.solidclaims.events.ClaimToolListener
import xyz.mizarc.solidclaims.events.ClaimVisualiser

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager
    internal var configIO: ConfigIO = ConfigIO(this)
    var database: DatabaseStorage = DatabaseStorage(this)
    var claimContainer = ClaimContainer(database)
    var ownerContainer = OwnerContainer()


    override fun onEnable() {
        database.openConnection()
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimContainer), this)
        server.pluginManager.registerEvents(ClaimToolListener(this.claimContainer), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this), this)
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(UnclaimCommand())
        commandManager.registerCommand(TrustCommand())
        commandManager.registerCommand(UntrustCommand())
        commandManager.registerCommand(InfoCommand())
        commandManager.registerCommand(TrustlistCommand())
        commandManager.registerCommand(PartitionlistCommand())
        commandManager.registerCommand(HandleEventsCommand())
        commandManager.commandCompletions.registerCompletion("permissions") {
            val perms: ArrayList<String> = ArrayList()
            for (p in ClaimPermission.values()) {
                perms.add(p.alias)
            }
            perms
        }
        loadDataFromDatabase()
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }

    private fun loadDataFromDatabase() {
        val claims = database.getAllClaims() ?: return
        for (claim in claims) {
            claimContainer.addClaim(claim)

            val claimPartitions = database.getClaimPartitionsByClaim(claim) ?: continue
            for (partition in claimPartitions) {
                claimContainer.addClaimPartition(partition)
            }
        }
    }
}
