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
    lateinit var database: DatabaseStorage
    lateinit var claimContainer: ClaimContainer
    lateinit var ownerContainer: OwnerContainer


    override fun onEnable() {
        logger.info("SolidClaims has been Enabled")
        claimContainer = ClaimContainer(database)
        ownerContainer = OwnerContainer()
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimContainer), this)
        server.pluginManager.registerEvents(ClaimToolListener(this.claimContainer), this)
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(HandleEventsCommand())
        loadDataFromDatabase()
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }

    private fun loadDataFromDatabase() {
        val claims = database.getAllClaims()
        for (claim in claims!!) {
            claimContainer.addClaim(claim)

            val claimPartitions = database.getClaimPartitionsByClaim(claim) ?: continue
            for (partition in claimPartitions) {
                claimContainer.addClaimPartition(partition)
            }
        }
    }
}
