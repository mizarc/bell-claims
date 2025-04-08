package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.results.PlayerPermissionChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import io.mockk.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class PlayerPermissionServiceImplTest {
    private lateinit var permissionRepo: PlayerAccessRepository
    private lateinit var playerPermissionService: PlayerPermissionServiceImpl

    private lateinit var uuidOne: UUID
    private lateinit var uuidTwo: UUID

    private lateinit var playerOne: OfflinePlayer
    private lateinit var playerTwo: OfflinePlayer

    private lateinit var claim: Claim

    @BeforeEach
    fun setup() {
        permissionRepo = mockk()
        playerPermissionService = PlayerPermissionServiceImpl(permissionRepo)

        playerOne = mockk<OfflinePlayer>()
        uuidOne = UUID.fromString("023d6d22-0ee8-4ccd-850a-08a378eb63b6")
        every { playerOne.uniqueId } returns uuidOne

        playerTwo = mockk<OfflinePlayer>()
        uuidTwo = UUID.fromString("6a3de442-b6c6-4686-8cde-97c6124d8c5a")
        every { playerTwo.uniqueId } returns uuidTwo

        claim = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "Test")
    }

    @Test
    fun `doesPlayerHavePermission - when player doesn't have permission - returns False`() {
        // Given
        val playerOnePermissions = setOf(ClaimPermission.Husbandry, ClaimPermission.Build)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns playerOnePermissions

        // When
        val result = playerPermissionService.doesPlayerHavePermission(claim, playerOne, ClaimPermission.VehicleDeploy)

        // Then
        assertFalse(result)
    }

    @Test
    fun `doesPlayerHavePermission - when player does have permission - returns True`() {
        // Given
        val playerOnePermissions = setOf(ClaimPermission.Husbandry, ClaimPermission.Build)
        val playerTwoPermissions = setOf(ClaimPermission.ContainerInspect,
            ClaimPermission.SignEdit, ClaimPermission.RedstoneInteract)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns playerOnePermissions

        // When
        val result = playerPermissionService.doesPlayerHavePermission(claim, playerOne, ClaimPermission.Build)

        // Then
        assertTrue(result)
    }

    @Test
    fun `getByClaim - returns expected Permission Map`() {
        // Given
        val playerOnePermissions = setOf(ClaimPermission.SignEdit, ClaimPermission.Build)
        val playerTwoPermissions = setOf(ClaimPermission.ContainerInspect,
            ClaimPermission.SignEdit, ClaimPermission.RedstoneInteract)
        val permissions = mapOf(uuidOne to playerOnePermissions, uuidTwo to playerTwoPermissions)
        mockkStatic(Bukkit::class)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { Bukkit.getOfflinePlayer(uuidOne) } returns playerOne
        every { Bukkit.getOfflinePlayer(uuidTwo) } returns playerTwo

        // When
        val result = playerPermissionService.getByClaim(claim)

        // Then
        assertEquals(mapOf(playerOne to playerOnePermissions, playerTwo to playerTwoPermissions), result)
    }

    @Test
    fun `getByPlayer - returns expected Permission Set`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.Build)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions

        // When
        val result = playerPermissionService.getByPlayer(claim, playerOne)

        // Then
        assertEquals(permissions, result)
    }

    @Test
    fun `addForPlayer - when player already has permission - returns UNCHANGED`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions

        // When
        val result = playerPermissionService.addForPlayer(claim, playerOne, ClaimPermission.ContainerInspect)

        // Then
        assertEquals(PlayerPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.add(claim, playerOne, any()) }
    }

    @Test
    fun `addForPlayer - when player doesn't have has permission - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.add(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.addForPlayer(claim, playerOne, ClaimPermission.Husbandry)

        // Then
        assertEquals(PlayerPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.add(claim, playerOne, any()) }
    }

    @Test
    fun `addAllForPlayer - when player has no permissions - returns SUCCESS`() {
        // Given
        every { permissionRepo.getByPlayer(claim, playerOne) } returns emptySet()
        every { permissionRepo.add(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.addAllForPlayer(claim, playerOne)

        // Then
        assertEquals(PlayerPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.add(claim, playerOne, any()) }
    }

    @Test
    fun `addAllForPlayer - when player has some permissions - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.add(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.addAllForPlayer(claim, playerOne)

        // Then
        assertEquals(PlayerPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.add(claim, playerOne, any()) }
    }

    @Test
    fun `addAllForPlayer - when player has all permissions - returns UNCHANGED`() {
        // Given
        val permissions = ClaimPermission.entries.toSet()
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.add(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.addAllForPlayer(claim, playerOne)

        // Then
        assertEquals(PlayerPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.add(claim, playerOne, any()) }
    }

    @Test
    fun `removeForPlayer - when player doesn't have the permission - returns UNCHANGED`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.remove(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.removeForPlayer(claim, playerOne, ClaimPermission.Build)

        // Then
        assertEquals(PlayerPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.remove(claim, playerOne, any()) }
    }

    @Test
    fun `removeForPlayer - when player has the permission - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.remove(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.removeForPlayer(claim, playerOne, ClaimPermission.SignEdit)

        // Then
        assertEquals(PlayerPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.remove(claim, playerOne, any()) }
    }

    @Test
    fun `removeAllForPlayer - when player has no permissions - returns UNCHANGED`() {
        // Given
        every { permissionRepo.getByPlayer(claim, playerOne) } returns emptySet()
        every { permissionRepo.remove(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.removeAllForPlayer(claim, playerOne)

        // Then
        assertEquals(PlayerPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.remove(claim, playerOne, any()) }
    }

    @Test
    fun `removeAllForPlayer - when player has some permissions - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.remove(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.removeAllForPlayer(claim, playerOne)

        // Then
        assertEquals(PlayerPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.remove(claim, playerOne, any()) }
    }

    @Test
    fun `removeAllForPlayer - when player has all permissions - returns SUCCESS`() {
        // Given
        val permissions = ClaimPermission.entries.toSet()
        every { permissionRepo.getByPlayer(claim, playerOne) } returns permissions
        every { permissionRepo.remove(claim, playerOne, any()) } just runs

        // When
        val result = playerPermissionService.removeAllForPlayer(claim, playerOne)

        // Then
        assertEquals(PlayerPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.remove(claim, playerOne, any()) }
    }
}