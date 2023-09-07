package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import net.kyori.adventure.text.BlockNBTComponent.Pos
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PartitionServiceImplTest {
    private lateinit var config: Config
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var claimService: ClaimService
    private lateinit var playerStateService: PlayerStateService
    private lateinit var partitionService: PartitionService

    private val playerOne = mockk<OfflinePlayer>()
    private val playerTwo = mockk<OfflinePlayer>()
    private val playerThree = mockk<OfflinePlayer>()

    private val claim = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "Test")
    private val partitionCollection = arrayOf(
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(8, 5), Position2D(19, 16))),
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(16, 17), Position2D(25, 24)))
    )

    private val otherClaim = Claim(UUID.randomUUID(), playerTwo, Position3D(21,74,30), "Test1")
    private val otherPartitionCollection = arrayOf(
        Partition(UUID.randomUUID(), otherClaim.id, Area(Position2D(17, 29), Position2D(23, 39))),
        Partition(UUID.randomUUID(), otherClaim.id, Area(Position2D(11, 37), Position2D(16, 43)))
    )

    @BeforeEach
    fun setup() {
        config = mockk()
        partitionRepo = mockk()
        claimService = mockk()
        playerStateService = mockk()
        partitionService = PartitionServiceImpl(config, partitionRepo, claimService, playerStateService)
    }

    @Test
    fun `isAreaValid (World) - when the area overlaps an existing partition - returns False`() {
        // Given
        val testArea = Area(Position2D(4, 3), Position2D(17, 9))
        val world = mockk<World>()
        every { world.uid } returns claim.worldId
        every { partitionRepo.getByChunk(any()) } returns setOf(partitionCollection[1])
        every { claimService.getById(claim.id) } returns claim
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, world)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAreaValid (World) - when the area is too close to an existing partition - returns False`() {
        // Given
        val testArea = Area(Position2D(-4, 3), Position2D(6, 9))
        val world = mockk<World>()
        every { world.uid } returns claim.worldId
        every { partitionRepo.getByChunk(any()) } returns setOf(partitionCollection[1])
        every { claimService.getById(claim.id) } returns claim
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, world)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAreaValid (World) - when the area is in a valid spot - returns True`() {
        // Given
        val testArea = Area(Position2D(-10, 3), Position2D(2, 9))
        val world = mockk<World>()
        every { world.uid } returns claim.worldId
        every { partitionRepo.getByChunk(any()) } returns setOf(partitionCollection[1])
        every { claimService.getById(claim.id) } returns claim
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, world)

        // Then
        assertTrue(result)
    }

    @Test
    fun testIsAreaValid() {
    }

    @Test
    fun isRemoveAllowed() {
    }

    @Test
    fun getById() {
    }

    @Test
    fun getByLocation() {
    }

    @Test
    fun getByChunk() {
    }

    @Test
    fun getByClaim() {
    }

    @Test
    fun getPrimary() {
    }

    @Test
    fun append() {
    }

    @Test
    fun resize() {
    }

    @Test
    fun delete() {
    }
}