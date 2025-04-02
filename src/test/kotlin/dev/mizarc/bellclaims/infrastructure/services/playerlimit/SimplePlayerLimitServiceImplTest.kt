package dev.mizarc.bellclaims.infrastructure.services.playerlimit

import dev.mizarc.bellclaims.application.services.PlayerLimitService
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.domain.values.*
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class SimplePlayerLimitServiceImplTest {
    private lateinit var config: Config
    private lateinit var metadata: Chat
    private lateinit var playerStateRepo: PlayerStateRepository
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var playerLimitService: PlayerLimitService

    private lateinit var playerOne: OfflinePlayer
    private lateinit var playerTwo: OfflinePlayer
    private lateinit var playerThree: OfflinePlayer

    private lateinit var uuidOne: UUID
    private lateinit var uuidTwo: UUID
    private lateinit var uuidThree: UUID

    private lateinit var world: World

    private lateinit var claimOne: Claim
    private lateinit var partitionCollectionOne: List<Partition>

    private lateinit var claimTwo: Claim
    private lateinit var partitionCollectionTwo: List<Partition>

    @BeforeEach
    fun setup() {
        config = mockk()
        metadata = mockk()
        playerStateRepo = mockk()
        claimRepo = mockk()
        partitionRepo = mockk()
        playerLimitService = SimplePlayerLimitServiceImpl(config, claimRepo, partitionRepo)

        playerOne = mockk<OfflinePlayer>()
        uuidOne = UUID.fromString("22d7b7e7-7773-4f78-8e0b-817960fba37a")
        every { playerOne.uniqueId } returns uuidOne

        playerTwo = mockk<OfflinePlayer>()
        uuidTwo = UUID.fromString("edd513e2-7dd1-4f40-9fcd-4911c198b204")
        every { playerTwo.uniqueId } returns uuidTwo

        playerThree = mockk<OfflinePlayer>()
        uuidThree = UUID.fromString("6211420c-fa72-481a-a237-2e9f8eb8abe0")
        every { playerThree.uniqueId } returns uuidThree

        world = mockk()
        every { world.uid } returns UUID.fromString("9e105325-aa3d-4272-8712-350b2b9f5fcc")

        claimOne = Claim(world.uid, playerOne, Position3D(15,85,10), "ClaimOne")
        partitionCollectionOne = listOf(
            Partition(UUID.randomUUID(), claimOne.id, Area(Position2D(17, 29), Position2D(23, 39))),
            Partition(UUID.randomUUID(), claimOne.id, Area(Position2D(11, 37), Position2D(16, 43)))
        )

        claimTwo = Claim(world.uid, playerOne, Position3D(21,74,30), "ClaimTwo")
        partitionCollectionTwo = listOf(
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(17, 29), Position2D(23, 39))),
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(11, 37), Position2D(16, 43)))
        )
    }

    @Test
    fun `getTotalClaimCount - when limit is below 0 - return Int of 0`() {
        // Given
        mockkStatic(Bukkit::class)
        every { config.claimLimit } returns -2

        // When
        val result = playerLimitService.getTotalClaimCount(playerOne)

        // Then
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `getTotalClaimCount - when limit is valid - return Int`() {
        // Given
        mockkStatic(Bukkit::class)
        every { config.claimLimit } returns 5

        // When
        val result = playerLimitService.getTotalClaimCount(playerOne)

        // Then
        Assertions.assertEquals(5, result)
    }

    @Test
    fun `getTotalClaimBlockCount - when limit is below 0 - return Int of 0`() {
        // Given
        mockkStatic(Bukkit::class)
        every { config.claimBlockLimit } returns -2000

        // When
        val result = playerLimitService.getTotalClaimBlockCount(playerOne)

        // Then
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `getTotalClaimBlockCount - when limit is valid - return Int`() {
        // Given
        mockkStatic(Bukkit::class)
        every { config.claimBlockLimit } returns 5000

        // When
        val result = playerLimitService.getTotalClaimBlockCount(playerOne)

        // Then
        Assertions.assertEquals(5000, result)
    }

    @Test
    fun `getUsedClaimsCount - when player has no claims - return Int of 0`() {
        // Given
        every { claimRepo.getByPlayer(playerOne) } returns setOf()

        // When
        val result = playerLimitService.getUsedClaimsCount(playerOne)

        // Then
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `getUsedClaimsCount - when player has claims - return Int of amount`() {
        // Given
        every { claimRepo.getByPlayer(playerOne) } returns setOf(claimOne, claimTwo)

        // When
        val result = playerLimitService.getUsedClaimsCount(playerOne)

        // Then
        Assertions.assertEquals(2, result)
    }

    @Test
    fun `getUsedClaimBlockCount - when player has no claims - return Int of 0`() {
        // Given
        every { claimRepo.getByPlayer(playerOne) } returns setOf()

        // When
        val result = playerLimitService.getUsedClaimBlockCount(playerOne)

        // Then
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `getUsedClaimBlockCount - when player has claims with partitions - return Int of amount`() {
        // Given
        every { claimRepo.getByPlayer(playerOne) } returns setOf(claimOne, claimTwo)
        every { partitionRepo.getByClaim(claimOne) } returns partitionCollectionOne.toSet()
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()

        // When
        val result = playerLimitService.getUsedClaimBlockCount(playerOne)

        // Then
        Assertions.assertEquals(238, result)
    }

    @Test
    fun `getRemainingClaimCount - return Int`() {
        // Given
        mockkStatic(Bukkit::class)
        every { config.claimLimit } returns 5
        every { claimRepo.getByPlayer(playerOne) } returns setOf(claimOne, claimTwo)

        // When
        val result = playerLimitService.getRemainingClaimCount(playerOne)

        // Then
        Assertions.assertEquals(3, result)
    }

    @Test
    fun `getRemainingClaimBlockCount - return Int`() {
        // Given
        mockkStatic(Bukkit::class)
        every { config.claimBlockLimit } returns 5000
        every { claimRepo.getByPlayer(playerOne) } returns setOf(claimOne, claimTwo)
        every { partitionRepo.getByClaim(claimOne) } returns partitionCollectionOne.toSet()
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()

        // When
        val result = playerLimitService.getRemainingClaimBlockCount(playerOne)

        // Then
        Assertions.assertEquals(4762, result)
    }
}