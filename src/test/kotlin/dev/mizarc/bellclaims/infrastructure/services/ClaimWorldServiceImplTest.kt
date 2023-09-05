package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimWorldService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

class ClaimWorldServiceImplTest {
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionService: PartitionService
    private lateinit var playerStateService: PlayerStateService
    private lateinit var claimWorldService: ClaimWorldService

    private val playerOne = mockk<OfflinePlayer>()
    private val playerTwo = mockk<OfflinePlayer>()
    private val playerThree = mockk<OfflinePlayer>()
    private val claim = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "Test")

    private val partitionCollection = arrayOf(
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(8, 5), Position2D(19, 16))),
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(16, 17), Position2D(25, 24))))

    @BeforeEach
    fun setup() {
        claimRepo = mockk()
        partitionService = mockk()
        playerStateService = mockk()
        claimWorldService = ClaimWorldServiceImpl(claimRepo, partitionService, playerStateService)
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
    fun isMoveLocationValid() {
    }

    @Test
    fun getByLocation() {
    }

    @Test
    fun create() {
    }

    @Test
    fun move() {
    }
}