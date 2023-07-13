package xyz.mizarc.solidclaims

import co.aikar.commands.PaperCommandManager
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.claims.*
import xyz.mizarc.solidclaims.commands.*
import xyz.mizarc.solidclaims.listeners.*
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.players.PlayerStateRepository
import xyz.mizarc.solidclaims.storage.DatabaseStorage

class SolidClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    internal var config: Config = Config(this)
    val storage = DatabaseStorage(this)
    val claimRepository = ClaimRepository(storage)
    val partitionRepository = PartitionRepository(storage)
    val claimPermissionRepository = ClaimPermissionRepository(storage)
    val claimRuleRepository = ClaimRuleRepository(storage)
    val playerAccessRepository = PlayerAccessRepository(storage)
    val playerStateRepository = PlayerStateRepository()
    var claimQuery = ClaimQuery(claimRepository, partitionRepository, claimRuleRepository, playerStateRepository)
    var claimVisualiser = ClaimVisualiser(this, claimQuery)

    override fun onEnable() {
        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager.getRegistration(Chat::class.java)!!
        commandManager = PaperCommandManager(this)
        metadata = serviceProvider.provider
        registerDependencies()
        registerCommands()
        registerEvents()
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }

    private fun registerDependencies() {
        commandManager.registerDependency(ClaimRepository::class.java, claimRepository)
        commandManager.registerDependency(PartitionRepository::class.java, partitionRepository)
        commandManager.registerDependency(PlayerStateRepository::class.java, playerStateRepository)
        commandManager.registerDependency(ClaimVisualiser::class.java, claimVisualiser)
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
        commandManager.registerCommand(AddRuleCommand())
        commandManager.registerCommand(RemoveRuleCommand())
        commandManager.registerCommand(ClaimOverrideCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimRepository, partitionRepository,
            claimRuleRepository, claimPermissionRepository, playerAccessRepository, playerStateRepository, claimQuery),
            this)
        server.pluginManager.registerEvents(ClaimToolListener(claimRepository, partitionRepository,
            playerStateRepository, claimQuery, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this, claimQuery), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(config, metadata,
            playerStateRepository), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
        server.pluginManager.registerEvents(ClaimManagementListener(claimRepository, partitionRepository, claimRuleRepository, playerAccessRepository), this)
    }
}
