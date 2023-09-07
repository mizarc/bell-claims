package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import io.mockk.every
import io.mockk.mockk
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.assertEquals
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

    private lateinit var world: World

    private lateinit var claimOne: Claim
    private lateinit var partitionCollectionOne: List<Partition>

    private lateinit var claimTwo: Claim
    private lateinit var partitionCollectionTwo: List<Partition>

    @BeforeEach
    fun setup() {
        config = mockk()
        partitionRepo = mockk()
        claimService = mockk()
        playerStateService = mockk()
        partitionService = PartitionServiceImpl(config, partitionRepo, claimService, playerStateService)

        // World Placeholder
        world = mockk<World>()
        every { world.uid } returns UUID.randomUUID()

        // Claim One
        claimOne = Claim(world.uid, playerOne, Position3D(15,85,10), "Test")
        partitionCollectionOne = listOf(
            Partition(UUID.randomUUID(), claimOne.id, Area(Position2D(8, 5), Position2D(19, 16))),
            Partition(UUID.randomUUID(), claimOne.id, Area(Position2D(16, 17), Position2D(25, 24))))

        claimTwo = Claim(world.uid, playerTwo, Position3D(21,74,30), "Test1")
        partitionCollectionTwo = listOf(
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(17, 29), Position2D(23, 39))),
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(11, 37), Position2D(16, 43))))
    }

    @Test
    fun `isAreaValid (World) - when the area overlaps an existing partition - returns False`() {
        // Given
        val testArea = Area(Position2D(4, 3), Position2D(17, 9))
        every { world.uid } returns claimOne.worldId
        every { partitionRepo.getByChunk(any()) } returns partitionCollectionOne.toSet()
        every { claimService.getById(claimOne.id) } returns claimOne
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, world)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAreaValid (World) - when the area is too close to an existing partition - returns False`() {
        // Given
        val testArea = Area(Position2D(-4, 3), Position2D(5, 9))
        every { partitionRepo.getByChunk(any()) } returns partitionCollectionOne.toSet()
        every { claimService.getById(claimOne.id) } returns claimOne
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
        every { partitionRepo.getByChunk(any()) } returns partitionCollectionOne.toSet()
        every { claimService.getById(claimOne.id) } returns claimOne
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, world)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isAreaValid (Claim) - when the area overlaps an existing partition - returns False`() {
        // Given
        val testArea = Area(Position2D(15, 22), Position2D(22, 28))
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, claimTwo)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAreaValid (Claim) - when the area is too close to an existing partition - returns False`() {
        // Given
        val testArea = Area(Position2D(9, 26), Position2D(16, 34))
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, claimTwo)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAreaValid (Claim) - when the area is in a valid spot - returns True`() {
        // Given
        val testArea = Area(Position2D(24, 38), Position2D(32, 50))
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.isAreaValid(testArea, claimTwo)

        // Then
        assertTrue(result)
    }

    @Test
    fun isRemoveAllowed() {
    }

    @Test
    fun getById() {
        // Given
        every { partitionRepo.getById(partitionCollectionOne[0].id) } returns partitionCollectionOne[0]

        // When
        val result = partitionService.getById(partitionCollectionOne[0].id)

        // Then
        assertEquals(partitionCollectionOne[0], result)
    }

    @Test
    fun getByLocation() {
        // Given
        //val location = Location()
        every { partitionRepo.getById(partitionCollectionOne[0].id) } returns partitionCollectionOne[0]

        // When
        //val result = partitionService.getByLocation()

        // Then
        //assertEquals(partitionCollection[0], result)
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