package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.ClaimWorldService
import dev.mizarc.bellclaims.application.services.PartitionService
import dev.mizarc.bellclaims.application.services.PlayerLimitService
import dev.mizarc.bellclaims.application.results.ClaimCreationResult
import dev.mizarc.bellclaims.application.results.ClaimMoveResult
import dev.mizarc.bellclaims.application.results.PartitionCreationResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import io.mockk.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

class ClaimWorldServiceImplTest {
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionService: PartitionService
    private lateinit var playerLimitService: PlayerLimitService
    private lateinit var claimWorldService: ClaimWorldService
    private lateinit var config: Config

    private lateinit var playerOne: OfflinePlayer
    private lateinit var playerTwo: OfflinePlayer
    private lateinit var playerThree: OfflinePlayer

    private lateinit var claimOne: Claim
    private lateinit var partitionCollectionOne: Array<Partition>

    private lateinit var claimTwo: Claim
    private lateinit var partitionCollectionTwo: Array<Partition>

    @BeforeEach
    fun setup() {
        config = mockk()
        every { config.minimumPartitionSize } returns 3
        every { config.distanceBetweenClaims } returns 1
        every { config.initialClaimSize } returns 7
        every { config.claimLimit } returns 3
        every { config.claimBlockLimit } returns 50000

        claimRepo = mockk()
        partitionService = mockk()
        playerLimitService = mockk()
        claimWorldService = ClaimWorldServiceImpl(claimRepo, partitionService, playerLimitService, config)

        playerOne = mockk<OfflinePlayer>()
        playerTwo = mockk<OfflinePlayer>()
        playerThree = mockk<OfflinePlayer>()

        claimOne = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "ClaimOne")
        partitionCollectionOne = arrayOf(
            Partition(UUID.randomUUID(), claimOne.id, Area(Position2D(8, 5), Position2D(19, 16))),
            Partition(UUID.randomUUID(), claimOne.id, Area(Position2D(16, 17), Position2D(25, 24))))

        claimTwo = Claim(UUID.randomUUID(), playerTwo, Position3D(21,74,30), "ClaimTwo")
        partitionCollectionTwo = arrayOf(
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(17, 29), Position2D(23, 39))),
            Partition(UUID.randomUUID(), claimTwo.id, Area(Position2D(11, 37), Position2D(16, 43))))


    }

    @Test
    fun `isNewLocationValid - when bell is far enough away from any existing claims - returns True`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 21.5, 74.0, 36.5)
        val defaultClaimArea = Area(
            Position2D(location.blockX - 5, location.blockZ - 5),
            Position2D(location.blockX + 5, location.blockZ + 5))
        every { partitionService.isAreaValid(defaultClaimArea, world) } returns true

        // When
        val result = claimWorldService.isNewLocationValid(location)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isNewLocationValid - when bell is placed too close to existing claim - returns False`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 21.5, 74.0, 30.5)
        val defaultClaimArea = Area(
            Position2D(location.blockX - 5, location.blockZ - 5),
            Position2D(location.blockX + 5, location.blockZ + 5))
        every { partitionService.isAreaValid(defaultClaimArea, world) } returns false

        // When
        val result = claimWorldService.isNewLocationValid(location)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isMoveLocationValid - when bell is placed within bounds of its own partitions - returns True`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 13.5, 73.0, 40.5)
        every { partitionService.getByLocation(location) } returns partitionCollectionOne[0]

        // When
        val result = claimWorldService.isMoveLocationValid(claimOne, location)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isMoveLocationValid - when bell is placed within another claim's bounds - returns False`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 22.5, 71.0, 19.5)
        every { partitionService.getByLocation(location) } returns partitionCollectionTwo[0]

        // When
        val result = claimWorldService.isMoveLocationValid(claimOne, location)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isMoveLocationValid - when bell is placed outside of any partition - returns False`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 19.5, 72.0, 27.5)
        every { partitionService.getByLocation(location) } returns null

        // When
        val result = claimWorldService.isMoveLocationValid(claimOne, location)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getByLocation - when claim bell does not exist at location - returns null`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 19.5, 72.0, 27.5)
        every { claimRepo.getByPosition(Position3D(19, 72, 27), world.uid) } returns null

        // When
        val result = claimWorldService.getByLocation(location)

        // Then
        assertNull(result)
    }

    @Test
    fun `getByLocation - when claim bell exists at location - returns claim object`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 21.5, 74.0, 30.5)
        every { claimRepo.getByPosition(Position3D(21, 74, 30), world.uid) } returns claimOne

        // When
        val result = claimWorldService.getByLocation(location)

        // Then
        assertEquals(claimOne, result)
    }

    @Test
    fun `create - when bell does not exist at location - return NOT_A_BELL`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 48.5, 75.0, 32.3)
        every { location.block.type } returns Material.OAK_LOG

        // When
        val result = claimWorldService.create("New", location, playerThree)

        // Then
        assertEquals(ClaimCreationResult.NOT_A_BELL, result)
    }

    @Test
    fun `create - when location isn't valid, conflicts with an existing claim - return TOO_CLOSE`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 48.5, 75.0, 32.3)
        every { location.block.type } returns Material.BELL
        every { partitionService.isAreaValid(any(), location.world) } returns false

        // When
        val result = claimWorldService.create("New", location, playerThree)


        // Then
        assertEquals(ClaimCreationResult.TOO_CLOSE, result)
    }

    @Test
    fun `create - when player has run out of claims - return OUT_OF_CLAIMS`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 48.5, 75.0, 32.3)
        every { location.block.type } returns Material.BELL
        every { partitionService.isAreaValid(any(), location.world) } returns true
        every { playerLimitService.getRemainingClaimCount(playerThree) } returns 0

        // When
        val result = claimWorldService.create("New", location, playerThree)

        // Then
        assertEquals(ClaimCreationResult.OUT_OF_CLAIMS, result)
    }

    @Test
    fun `create - when player is out of claims - return OUT_OF_CLAIMS`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 48.5, 75.0, 32.3)
        every { location.block.type } returns Material.BELL
        every { partitionService.isAreaValid(any(), location.world) } returns true
        every { playerLimitService.getRemainingClaimCount(playerThree) } returns 0

        // When
        val result = claimWorldService.create("New", location, playerThree)

        // Then
        assertEquals(ClaimCreationResult.OUT_OF_CLAIMS, result)
    }

    @Test
    fun `create - when player doesn't have enough claim blocks - return OUT_OF_CLAIM_BLOCKS`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 48.5, 75.0, 32.3)
        every { location.block.type } returns Material.BELL
        every { partitionService.isAreaValid(any(), location.world) } returns true
        every { playerLimitService.getRemainingClaimCount(playerThree) } returns 1
        every { playerLimitService.getRemainingClaimBlockCount(playerThree) } returns 12

        // When
        val result = claimWorldService.create("New", location, playerThree)

        // Then
        assertEquals(ClaimCreationResult.OUT_OF_CLAIM_BLOCKS, result)
    }

    @Test
    fun `create - when all requirements are met - return SUCCESS`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 48.5, 75.0, 32.3)
        every { location.block.type } returns Material.BELL
        every { partitionService.isAreaValid(any(), location.world) } returns true
        every { playerLimitService.getRemainingClaimCount(playerThree) } returns 1
        every { playerLimitService.getRemainingClaimBlockCount(playerThree) } returns 121
        every { world.uid } returns UUID.randomUUID()
        every { claimRepo.add(any()) } returns Unit
        every { partitionService.append(any(), any()) } returns PartitionCreationResult.SUCCESS

        // When
        val result = claimWorldService.create("New", location, playerThree)

        // Then
        assertEquals(ClaimCreationResult.SUCCESS, result)
    }

    @Test
    fun `move - when location is outside of the claim partitions - return OUTSIDE_OF_AREA`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 44.5, 72.0, 19.5)
        every { claimWorldService.isMoveLocationValid(claimOne, location) } returns false
        every { claimRepo.update(claimOne) } returns Unit

        // When
        val result = claimWorldService.move(claimOne, location)

        // Then
        assertEquals(ClaimMoveResult.OUTSIDE_OF_AREA, result)
    }

    @Test
    fun `move - when location is valid - return SUCCESS`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 46.5, 74.0, 31.5)
        every { claimWorldService.isMoveLocationValid(claimOne, location) } returns true
        every { claimRepo.update(claimOne) } returns Unit

        // When
        val result = claimWorldService.move(claimOne, location)

        // Then
        assertEquals(ClaimMoveResult.SUCCESS, result)
    }
}