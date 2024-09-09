package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerLimitService
import dev.mizarc.bellclaims.api.enums.PartitionCreationResult
import dev.mizarc.bellclaims.api.enums.PartitionDestroyResult
import dev.mizarc.bellclaims.api.enums.PartitionResizeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import io.mockk.*
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PartitionServiceImplTest {
    private lateinit var config: Config
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var claimService: ClaimService
    private lateinit var playerLimitService: PlayerLimitService
    private lateinit var partitionService: PartitionService

    private val playerOne = mockk<OfflinePlayer>()
    private val playerTwo = mockk<OfflinePlayer>()

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
        playerLimitService = mockk()
        partitionService = PartitionServiceImpl(config, partitionRepo, claimService, playerLimitService)

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
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(11, 37), Position2D(16, 43))),
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(15, 44), Position2D(25, 53))))
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
        val testArea = Area(Position2D(24, 54), Position2D(32, 69))
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
    fun `getByLocation - when location is outside of any partition - return null`() {
        // Given
        //val location = Location()
        every { partitionRepo.getByPosition(Position2D(10, 22)) } returns setOf()
        every { claimService.getById(claimOne.id) } returns null

        // When
        val result = partitionService.getByLocation(Location(world, 10.5, 71.0, 22.5))

        // Then
        assertNull(result)
    }

    @Test
    fun `getByLocation - when location is inside of a partition - return found Partition`() {
        // Given
        every { partitionRepo.getByPosition(Position2D(17, 14)) } returns setOf(partitionCollectionOne[0])
        every { claimService.getById(claimOne.id) } returns claimOne

        // When
        val result = partitionService.getByLocation(Location(world, 17.5, 71.0, 14.5))

        // Then
        assertEquals(partitionCollectionOne[0], result)
    }

    @Test
    fun `getByChunk - when no partitions exist in the chunk - return empty Set`() {
        // Given
        val chunk = mockk<Chunk>()
        every { chunk.world } returns world
        every { chunk.x } returns 5
        every { chunk.z } returns 5
        every { partitionRepo.getByChunk(Position2D(5, 5)) } returns setOf()
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo

        // When
        val result = partitionService.getByChunk(chunk)

        // Then
        assertEquals(result, setOf())
    }

    @Test
    fun `getByChunk - when partitions exist in the chunk - return Set of Partition`() {
        // Given
        val chunk = mockk<Chunk>()
        every { chunk.world } returns world
        every { chunk.x } returns 1
        every { chunk.z } returns 1
        every { partitionRepo.getByChunk(Position2D(1, 1)) } returns
                setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0])
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo

        // When
        val result = partitionService.getByChunk(chunk)

        // Then
        assertEquals(result, setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0]))
    }

    @Test
    fun `getByClaim - when claim is selected - returns Set of Partition`() {
        // Given
        every { partitionRepo.getByClaim(claimOne) } returns partitionCollectionOne.toSet()

        // When
        val result = partitionService.getByClaim(claimOne)

        // Then
        assertEquals(partitionCollectionOne.toSet(), result)
    }

    @Test
    fun `getPrimary - when claim exists inside partition - return Partition`() {
        // Given
        every { partitionRepo.getByPosition(claimOne.position) } returns setOf(partitionCollectionOne[0])
        every { claimService.getById(claimOne.id) } returns claimOne

        // When
        val result = partitionService.getPrimary(claimOne)

        // Then
        assertEquals(partitionCollectionOne[0], result)
    }

    @Test
    fun `append - when area overlaps another claim's partition - return OVERLAP`() {
        // Given
        val area = Area(Position2D(14, 20), Position2D(23, 28))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByChunk(any()) } returns
        setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0])

        // When
        val result = partitionService.append(area, claimTwo)

        // Then
        assertEquals(PartitionCreationResult.OVERLAP, result)
        verify(exactly = 0) { partitionRepo.add(any()) }
    }

    @Test
    fun `append - when area too close to another claim's partition - return TOO_CLOSE`() {
        // Given
        val area = Area(Position2D(11, 27), Position2D(16, 34))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByChunk(any()) } returns
                setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0])
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.append(area, claimTwo)

        // Then
        assertEquals(PartitionCreationResult.TOO_CLOSE, result)
        verify(exactly = 0) { partitionRepo.add(any()) }
    }

    @Test
    fun `append - when area is too small (less than 3x3) - return TOO_SMALL`() {
        // Given
        val area = Area(Position2D(20, 40), Position2D(22, 42))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByChunk(any()) } returns
                setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0])
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.append(area, claimTwo)

        // Then
        assertEquals(PartitionCreationResult.TOO_SMALL, result)
        verify(exactly = 0) { partitionRepo.add(any()) }
    }

    @Test
    fun `append - when player doesn't have enough claim blocks - return INSUFFICIENT_BLOCKS`() {
        // Given
        val area = Area(Position2D(24, 37), Position2D(35, 51))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByChunk(any()) } returns
                setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0])
        every { config.distanceBetweenClaims } returns 3
        every { playerLimitService.getRemainingClaimBlockCount(playerTwo) } returns 10

        // When
        val result = partitionService.append(area, claimTwo)

        // Then
        assertEquals(PartitionCreationResult.INSUFFICIENT_BLOCKS, result)
        verify(exactly = 0) { partitionRepo.add(any()) }
    }

    @Test
    fun `append - when area is valid - return SUCCESS`() {
        // Given
        val area = Area(Position2D(24, 37), Position2D(35, 51))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByChunk(any()) } returns
                setOf(partitionCollectionOne[0], partitionCollectionOne[1], partitionCollectionTwo[0])
        every { config.distanceBetweenClaims } returns 3
        every { playerLimitService.getRemainingClaimBlockCount(playerTwo) } returns 9999
        every { partitionRepo.add(any()) } just runs

        // When
        val result = partitionService.append(area, claimTwo)

        // Then
        assertEquals(PartitionCreationResult.SUCCESS, result)
        verify { partitionRepo.add(any()) }
    }

    @Test
    fun `resize - when resizing the partition would overlap an existing partition - return DISCONNECTED`() {
        // Given
        val area = Area(Position2D(11, 37), Position2D(19, 47))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()

        // When
        val result = partitionService.resize(partitionCollectionTwo[1], area)

        // Then
        assertEquals(PartitionResizeResult.OVERLAP, result)
        verify(exactly = 0) { partitionRepo.update(any()) }
    }

    @Test
    fun `resize - when the partition being resized is too close to another claim's partition - return TOO_CLOSE`() {
        // Given
        val area = Area(Position2D(17, 26), Position2D(23, 39))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.resize(partitionCollectionTwo[0], area)

        // Then
        assertEquals(PartitionResizeResult.TOO_CLOSE, result)
        verify(exactly = 0) { partitionRepo.update(any()) }
    }

    @Test
    fun `resize - when resizing the partition would result in disconnected partitions - return DISCONNECTED`() {
        // Given
        val area = Area(Position2D(11, 37), Position2D(16, 41))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.resize(partitionCollectionTwo[1], area)

        // Then
        assertEquals(PartitionResizeResult.DISCONNECTED, result)
        verify(exactly = 0) { partitionRepo.update(any()) }
    }

    @Test
    fun `resize - when resizing would cause the claim bell to be outside the partition - return EXPOSED_CLAIM_HUB`() {
        // Given
        val area = Area(Position2D(17, 32), Position2D(23, 39))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.resize(partitionCollectionTwo[0], area)

        // Then
        assertEquals(PartitionResizeResult.EXPOSED_CLAIM_HUB, result)
        verify(exactly = 0) { partitionRepo.update(any()) }
    }

    @Test
    fun `resize - when the resize makes the partition too small - return TOO_SMALL`() {
        // Given
        val area = Area(Position2D(13, 31), Position2D(14, 43))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { config.distanceBetweenClaims } returns 3

        // When
        val result = partitionService.resize(partitionCollectionTwo[1], area)

        // Then
        assertEquals(PartitionResizeResult.TOO_SMALL, result)
        verify(exactly = 0) { partitionRepo.update(any()) }
    }

    @Test
    fun `resize - when the player doesn't have enough claim blocks - return INSUFFICIENT_BLOCKS`() {
        // Given
        val area = Area(Position2D(8, 31), Position2D(16, 43))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { config.distanceBetweenClaims } returns 3
        every { playerLimitService.getTotalClaimBlockCount(playerTwo) } returns 250
        every { playerLimitService.getUsedClaimBlockCount(playerTwo) } returns 229

        // When
        val result = partitionService.resize(partitionCollectionTwo[1], area)

        // Then
        assertEquals(PartitionResizeResult.INSUFFICIENT_BLOCKS, result)
        verify(exactly = 0) { partitionRepo.update(any()) }
    }

    @Test
    fun `resize - when resize is valid - return SUCCESS`() {
        // Given
        val area = Area(Position2D(8, 31), Position2D(16, 43))
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.getByChunk(any()) } returns (partitionCollectionOne + partitionCollectionTwo).toSet()
        every { config.distanceBetweenClaims } returns 3
        every { playerLimitService.getTotalClaimBlockCount(playerTwo) } returns 500
        every { playerLimitService.getUsedClaimBlockCount(playerTwo) } returns 229
        every { partitionRepo.update(any()) } just runs

        // When
        val result = partitionService.resize(partitionCollectionTwo[1], area)

        // Then
        assertEquals(PartitionResizeResult.SUCCESS, result)
        verify { partitionRepo.update(any()) }
    }

    @Test
    fun `delete - when deletion would result in a disconnected partition - return DISCONNECTED`() {
        // Given
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])

        // When
        val result = partitionService.delete(partitionCollectionTwo[1])

        // Then
        assertEquals(PartitionDestroyResult.DISCONNECTED, result)
        verify(exactly = 0) { partitionRepo.remove(any()) }
    }

    @Test
    fun `delete - when deletion is valid - return SUCCESS`() {
        // Given
        every { claimService.getById(claimOne.id) } returns claimOne
        every { claimService.getById(claimTwo.id) } returns claimTwo
        every { partitionRepo.getByClaim(claimTwo) } returns partitionCollectionTwo.toSet()
        every { partitionRepo.getByPosition(Position2D(21, 30)) } returns setOf(partitionCollectionTwo[0])
        every { partitionRepo.remove(any()) } just runs

        // When
        val result = partitionService.delete(partitionCollectionTwo[2])

        // Then
        assertEquals(PartitionDestroyResult.SUCCESS, result)
        verify { partitionRepo.remove(any()) }
    }
}