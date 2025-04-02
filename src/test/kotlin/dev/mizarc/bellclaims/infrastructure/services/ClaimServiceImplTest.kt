package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.ClaimService
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.values.*
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.domain.entities.Partition
import io.mockk.*
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.assertEquals

class ClaimServiceImplTest {
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var claimFlagRepo: ClaimFlagRepository
    private lateinit var claimPermissionRepo: ClaimPermissionRepository
    private lateinit var playerPermissionRepo: PlayerAccessRepository
    private lateinit var claimService: ClaimService

    private val playerOne = mockk<OfflinePlayer>()
    private val playerTwo = mockk<OfflinePlayer>()
    private val playerThree = mockk<OfflinePlayer>()

    private lateinit var claim: Claim
    private lateinit var claimCollection: Array<Claim>
    private lateinit var partitionCollection: Array<Partition>

    @BeforeEach
    fun setup() {
        claimRepo = mockk()
        partitionRepo = mockk()
        claimFlagRepo = mockk()
        claimPermissionRepo = mockk()
        playerPermissionRepo = mockk()
        claimService = ClaimServiceImpl(claimRepo, partitionRepo,
            claimFlagRepo, claimPermissionRepo, playerPermissionRepo)

        claim = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "Test")
        claimCollection = arrayOf(
            Claim(UUID.randomUUID(), playerOne, Position3D(86,12,32), "Test 1"),
            Claim(UUID.randomUUID(), playerOne, Position3D(23,85,34), "Test 2"),
            Claim(UUID.randomUUID(), playerOne, Position3D(58,74,93), "Test 3"),
            Claim(UUID.randomUUID(), playerTwo, Position3D(11,5,40), "Test 4"),
            Claim(UUID.randomUUID(), playerTwo, Position3D(37,17,61), "Test 5"),
            Claim(UUID.randomUUID(), playerTwo, Position3D(67,88,59), "Test 6"),
            Claim(UUID.randomUUID(), playerThree, Position3D(72,24,87), "Test 7"),
            Claim(UUID.randomUUID(), playerThree, Position3D(18,15,77), "Test 8"),
            Claim(UUID.randomUUID(), playerThree, Position3D(46,36,83), "Test 9"))
        partitionCollection = arrayOf(
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(8, 5), Position2D(19, 16))),
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(16, 17), Position2D(25, 24))),
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(2, -5), Position2D(7, 8))),
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(-5, -6), Position2D(1, -1))),
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(-14, -9), Position2D(-6, -5)))
        )
    }

    @Test
    fun getById() {
        // Given
        every { claimRepo.getById(claim.id) } returns claim

        // When
        val result = claimService.getById(claim.id)

        // Then
        assertEquals(claim, result)
    }

    @Test
    fun getByPlayer() {
        // Given
        every { claimRepo.getByPlayer(playerOne) } returns claimCollection.slice(0..2).toSet()

        // When
        val result = claimService.getByPlayer(playerOne)

        // Then
        assertEquals(claimCollection.slice(0..2).toSet(), result)
    }

    @Test
    fun getBlockCount() {
        // Given
        every { partitionRepo.getByClaim(claim) } returns partitionCollection.toSet()

        // When
        val result = claimService.getBlockCount(claim)

        // Then
        assertEquals(395, result)
    }

    @Test
    fun getPartitionCount() {
        // Given
        every {partitionRepo.getByClaim(claim)} returns partitionCollection.toSet()

        // When
        val result = claimService.getPartitionCount(claim)

        // Then
        assertEquals(5, result)
    }

    @Test
    fun changeName() {
        // Given
        every { claimRepo.update(claim) } returns Unit

        // When
        claimService.changeName(claim, "Changed")

        // Then
        assertEquals("Changed", claim.name)
        verify { claimRepo.update(claim) }
    }

    @Test
    fun changeDescription() {
        // Given
        every { claimRepo.update(claim) } returns Unit

        // When
        claimService.changeDescription(claim, "Updated")

        // Then
        assertEquals("Updated", claim.description)
        verify { claimRepo.update(claim) }
    }

    @Test
    fun changeIcon() {
        // Given
        every { claimRepo.update(claim) } returns Unit

        // When
        claimService.changeIcon(claim, Material.ACACIA_BOAT)

        // Then
        assertEquals(Material.ACACIA_BOAT, claim.icon)
        verify { claimRepo.update(claim) }
    }

    @Test
    fun destroy() {
        // Given
        every { partitionRepo.removeByClaim(claim) } just Runs
        every { claimFlagRepo.removeByClaim(claim) } just Runs
        every { claimPermissionRepo.removeByClaim(claim) } just Runs
        every { playerPermissionRepo.removeByClaim(claim) } just Runs
        every { claimRepo.remove(claim) } just Runs

        // When
        claimService.destroy(claim)

        // Then
        verifyOrder {
            partitionRepo.removeByClaim(claim)
            claimFlagRepo.removeByClaim(claim)
            claimPermissionRepo.removeByClaim(claim)
            playerPermissionRepo.removeByClaim(claim)
            claimRepo.remove(claim)
        }
    }
}