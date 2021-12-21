package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.commands.*
import xyz.mizarc.solidclaims.events.*

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager
    internal var configIO: ConfigIO = ConfigIO(this)
    var database: DatabaseStorage = DatabaseStorage(this)
    var claimContainer = ClaimContainer(database)
    var playerContainer = PlayerContainer(database)
    var claimVisualiser = ClaimVisualiser(this)

    override fun onEnable() {
        database.openConnection()
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimContainer), this)
        server.pluginManager.registerEvents(ClaimToolListener(claimContainer, playerContainer), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(playerContainer), this)
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimlistCommand())
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(UnclaimCommand())
        commandManager.registerCommand(TrustCommand())
        commandManager.registerCommand(UntrustCommand())
        loadDataFromDatabase()
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }

    private fun loadDataFromDatabase() {
        val claims = database.getAllClaims()
        if (claims != null)
        {
            for (claim in claims) {
                claimContainer.addClaim(claim)

                val claimPartitions = database.getClaimPartitionsByClaim(claim) ?: continue
                for (partition in claimPartitions) {
                    claimContainer.addClaimPartition(partition)
                }
            }
        }

        val playerStates = database.getAllPlayerStates()
        if (playerStates != null) {
            for (playerState in playerStates) {
                playerContainer.addPlayer(playerState)
            }
        }
    }
}
