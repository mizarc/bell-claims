package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.flags.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.domain.permissions.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.permissions.PlayerAccessRepository
import io.mockk.every
import io.mockk.mockk
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
    private val claim = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "Test")
    private val claimCollection = arrayOf(
        Claim(UUID.randomUUID(), playerOne, Position3D(86,12,32), "Test 1"),
        Claim(UUID.randomUUID(), playerOne, Position3D(23,85,34), "Test 2"),
        Claim(UUID.randomUUID(), playerOne, Position3D(58,74,93), "Test 3"),
        Claim(UUID.randomUUID(), playerTwo, Position3D(11,5,40), "Test 4"),
        Claim(UUID.randomUUID(), playerTwo, Position3D(37,17,61), "Test 5"),
        Claim(UUID.randomUUID(), playerTwo, Position3D(67,88,59), "Test 6"),
        Claim(UUID.randomUUID(), playerThree, Position3D(72,24,87), "Test 7"),
        Claim(UUID.randomUUID(), playerThree, Position3D(18,15,77), "Test 8"),
        Claim(UUID.randomUUID(), playerThree, Position3D(46,36,83), "Test 9"))

    private val partitionCollection = arrayOf(
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(8, 5), Position2D(19, 16))),
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(16, 17), Position2D(25, 24))),
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(2, -5), Position2D(7, 8))),
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(-5, -6), Position2D(1, -1))),
        Partition(UUID.randomUUID(), claim.id, Area(Position2D(-14, -19), Position2D(-6, -5))))

    @BeforeEach
    fun setup() {
        claimRepo = mockk()
        partitionRepo = mockk()
        claimFlagRepo = mockk()
        claimPermissionRepo = mockk()
        playerPermissionRepo = mockk()
        claimService = ClaimServiceImpl(claimRepo, partitionRepo,
            claimFlagRepo, claimPermissionRepo, playerPermissionRepo)
    }

    @Test
    fun getById() {
        every { claimRepo.getById(claim.id) } returns claim

        assertEquals(claim, claimService.getById(claim.id))
    }

    @Test
    fun getByPlayer() {
        every { claimRepo.getByPlayer(playerOne) } returns claimCollection.slice(0..2).toSet()

        assertEquals(claimService.getByPlayer(playerOne), claimCollection.slice(0..2).toSet())
    }

    @Test
    fun getBlockCount() {
        every { partitionRepo.getByClaim(claim) } returns partitionCollection.toSet()

        assertEquals(395, claimService.getBlockCount(claim))
    }

    @Test
    fun getPartitionCount() {
    }

    @Test
    fun changeName() {
    }

    @Test
    fun changeDescription() {
    }

    @Test
    fun changeIcon() {
    }

    @Test
    fun destroy() {
    }
}