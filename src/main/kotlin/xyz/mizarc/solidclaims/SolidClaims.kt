package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.commands.*
import xyz.mizarc.solidclaims.events.*
import java.util.*

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
        server.pluginManager.registerEvents(ClaimToolListener(
            claimContainer, playerContainer, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(playerContainer), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(ClaimlistCommand())
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(UnclaimCommand())
        commandManager.registerCommand(TrustCommand())
        commandManager.registerCommand(PartitionlistCommand())
        commandManager.registerCommand(TrustlistCommand())
        commandManager.registerCommand(InfoCommand())
        commandManager.registerCommand(UntrustCommand())
        commandManager.registerCommand(RenameCommand())
        loadDataFromDatabase()
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }

    private fun loadDataFromDatabase() {
        val playerStates = database.getAllPlayerStates()
        if (playerStates != null) {

            // Add players
            for (playerState in playerStates) {
                playerContainer.addPlayer(playerState)

                // Add claims
                for (claim in playerState.claims) {
                    claimContainer.addClaim(claim)

                    // Add partitions
                    for (partition in claim.claimPartitions) {
                        claimContainer.addClaimPartition(partition)
                    }
                }
            }
        }
    }
}
