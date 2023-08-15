package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.api.claims.ClaimService
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.bellclaims.infrastructure.Config
import dev.mizarc.bellclaims.infrastructure.PartitionService
import dev.mizarc.bellclaims.infrastructure.claims.ClaimPermissionRepository
import dev.mizarc.bellclaims.infrastructure.claims.ClaimRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.claims.ClaimRuleRepository
import dev.mizarc.bellclaims.infrastructure.claims.PlayerAccessRepository
import dev.mizarc.bellclaims.infrastructure.ClaimServiceImpl
import dev.mizarc.bellclaims.infrastructure.partitions.PartitionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.players.PlayerStateRepository
import dev.mizarc.bellclaims.infrastructure.storage.DatabaseStorage
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*

class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    internal var config: Config = Config(this)
    val storage = DatabaseStorage(this)
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionRepo: PartitionRepository
    val claimPermissionRepo = ClaimPermissionRepository(storage)
    val claimRuleRepo = ClaimRuleRepository(storage)
    val playerAccessRepo = PlayerAccessRepository(storage)
    val playerStateRepo = PlayerStateRepository()
    private lateinit var claimService: ClaimService
    val partitionService = PartitionService(config, claimService, partitionRepo)
    val claimVisualiser = ClaimVisualiser(this, claimService, partitionService, playerStateRepo)

    override fun onEnable() {
        claimRepo = ClaimRepositorySQLite(storage)
        partitionRepo = PartitionRepositorySQLite(storage)
        claimService = ClaimServiceImpl(claimRepo, partitionRepo, claimRuleRepo,
            claimPermissionRepo, playerAccessRepo, playerStateRepo)
        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager.getRegistration(Chat::class.java)!!
        commandManager = PaperCommandManager(this)
        metadata = serviceProvider.provider
        registerDependencies()
        registerCommands()
        registerEvents()
        logger.info("Bell Claims has been Enabled")
    }

    override fun onDisable() {
        logger.info("Bell Claims has been Disabled")
    }

    private fun registerDependencies() {
        commandManager.registerDependency(ClaimRepositorySQLite::class.java, claimRepo)
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
        commandManager.registerCommand(ClaimMenuCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(
            ClaimEventHandler(this, claimRepo, partitionRepo,
            claimRuleRepo, claimPermissionRepo, playerAccessRepo, playerStateRepo, claimService, partitionService),
            this)
        server.pluginManager.registerEvents(
            ClaimToolListener(claimRepo, playerStateRepo, claimService,
            partitionService, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this, claimService, partitionService, playerStateRepo), this)
        server.pluginManager.registerEvents(
            PlayerRegistrationListener(config, metadata,
            playerStateRepo), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
        server.pluginManager.registerEvents(
            ClaimManagementListener(claimRepo, partitionRepo,
            claimRuleRepo, claimPermissionRepo, playerAccessRepo, partitionService, claimService), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(claimService, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimMoveListener(claimRepo, partitionService), this)
        server.pluginManager.registerEvents(ClaimMoveToolRemovalListener(), this)
        server.pluginManager.registerEvents(MiscPreventions(claimService, partitionService), this)
    }
}
