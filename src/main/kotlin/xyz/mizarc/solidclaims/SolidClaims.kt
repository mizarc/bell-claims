package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.commands.*
import xyz.mizarc.solidclaims.events.*

class SolidClaims : JavaPlugin() {
    internal lateinit var commandManager: PaperCommandManager
    internal var config: Config = Config(this)
    var database: DatabaseStorage = DatabaseStorage(this)
    var claimContainer = ClaimContainer(database)
    var playerContainer = PlayerContainer(database)
    var claimVisualiser = ClaimVisualiser(this)

    override fun onEnable() {
        database.openConnection()
        loadDataFromDatabase()

        commandManager = PaperCommandManager(this)
        registerDependencies()
        registerCommands()
        registerEvents()

        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }

    private fun registerDependencies() {
        commandManager.registerDependency(ClaimContainer::class.java, claimContainer)
        commandManager.registerDependency(PlayerContainer::class.java, playerContainer)
    }

    private fun registerCommands() {
        commandManager.registerCommand(ClaimlistCommand())
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(UnclaimCommand())
        commandManager.registerCommand(TrustCommand())
        commandManager.registerCommand(PartitionlistCommand())
        commandManager.registerCommand(TrustlistCommand())
        commandManager.registerCommand(InfoCommand())
        commandManager.registerCommand(UntrustCommand())
        commandManager.registerCommand(RenameCommand())
        commandManager.registerCommand(DescriptionCommand())
        commandManager.registerCommand(SetmainCommand())
        commandManager.registerCommand(AddRuleCommand())
        commandManager.registerCommand(RemoveRuleCommand())
        commandManager.registerCommand(ClaimOverrideCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimContainer), this)
        server.pluginManager.registerEvents(ClaimToolListener(claimContainer, playerContainer, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(playerContainer), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
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
                    for (partition in claim.partitions) {
                        claimContainer.addClaimPartition(partition)
                    }
                }
            }
        }
    }
}
