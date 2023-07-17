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
    val claimRepo = ClaimRepository(storage)
    val partitionRepo = PartitionRepository(storage)
    val claimPermissionRepo = ClaimPermissionRepository(storage)
    val claimRuleRepo = ClaimRuleRepository(storage)
    val playerAccessRepo = PlayerAccessRepository(storage)
    val playerStateRepo = PlayerStateRepository()
    val claimService = ClaimService(claimRepo, partitionRepo, claimRuleRepo, claimPermissionRepo,
        playerAccessRepo, playerStateRepo)
    val partitionService = PartitionService(claimService, partitionRepo)
    val claimVisualiser = ClaimVisualiser(this, claimService, partitionService)

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
        commandManager.registerDependency(ClaimRepository::class.java, claimRepo)
        commandManager.registerDependency(PartitionRepository::class.java, partitionRepo)
        commandManager.registerDependency(ClaimRuleRepository::class.java, claimRuleRepo)
        commandManager.registerDependency(ClaimPermissionRepository::class.java, claimPermissionRepo)
        commandManager.registerDependency(PlayerAccessRepository::class.java, playerAccessRepo)
        commandManager.registerDependency(PlayerStateRepository::class.java, playerStateRepo)
        commandManager.registerDependency(ClaimVisualiser::class.java, claimVisualiser)
        commandManager.registerDependency(ClaimService::class.java, claimService)
        commandManager.registerDependency(PartitionService::class.java, partitionService)
    }

    private fun registerCommands() {
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(ClaimlistCommand())
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
        server.pluginManager.registerEvents(ClaimEventHandler(this, claimRepo, partitionRepo,
            claimRuleRepo, claimPermissionRepo, playerAccessRepo, playerStateRepo, claimService, partitionService),
            this)
        server.pluginManager.registerEvents(ClaimToolListener(claimRepo, playerStateRepo, claimService,
            partitionService, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this, claimService, partitionService), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(config, metadata,
            playerStateRepo), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
        server.pluginManager.registerEvents(ClaimManagementListener(claimRepo, partitionRepo,
            claimRuleRepo, claimPermissionRepo, playerAccessRepo), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(claimService), this)
    }
}
