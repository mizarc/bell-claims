package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import io.mockk.*
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class DefaultPermissionServiceImplTest {
    private lateinit var permissionRepo: ClaimPermissionRepository
    private lateinit var defaultPermissionService: DefaultPermissionServiceImpl

    private lateinit var playerOne: OfflinePlayer
    private lateinit var claim: Claim

    @BeforeEach
    fun setup() {
        permissionRepo = mockk()
        defaultPermissionService = DefaultPermissionServiceImpl(permissionRepo)

        playerOne = mockk<OfflinePlayer>()
        claim = Claim(UUID.randomUUID(), playerOne, Position3D(15,85,10), "Test")
    }

    @Test
    fun `getByClaim - returns expected permissions`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions

        // When
        val result = defaultPermissionService.getByClaim(claim)

        // Then
        assertEquals(permissions, result)
    }

    @Test
    fun `add - when permission already linked to claim - returns UNCHANGED`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.add(claim, any()) } just runs

        // When
        val result = defaultPermissionService.add(claim, ClaimPermission.ContainerInspect)

        // Then
        assertEquals(DefaultPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.add(claim, any()) }
    }

    @Test
    fun `add - when permission is new - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.add(claim, any()) } just runs

        // When
        val result = defaultPermissionService.add(claim, ClaimPermission.Build)

        // Then
        assertEquals(DefaultPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.add(claim, any()) }
    }

    @Test
    fun `addAll - when no permissions are in repo - returns SUCCESS`() {
        // Given
        every { permissionRepo.getByClaim(claim) } returns emptySet()
        every { permissionRepo.add(claim, any()) } just runs

        // When
        val result = defaultPermissionService.addAll(claim)

        // Then
        assertEquals(DefaultPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.add(claim, any()) }
    }

    @Test
    fun `addAll - when some permissions are in repo - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.add(claim, any()) } just runs

        // When
        val result = defaultPermissionService.addAll(claim)

        // Then
        assertEquals(DefaultPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.add(claim, any()) }
    }

    @Test
    fun `addAll - when all permissions are in repo - returns UNCHANGED`() {
        // Given
        val permissions = ClaimPermission.entries.toSet()
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.add(claim, any()) } just runs

        // When
        val result = defaultPermissionService.addAll(claim)

        // Then
        assertEquals(DefaultPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.add(claim, any()) }
    }

    @Test
    fun `remove - when permission not linked to claim - returns UNCHANGED`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.remove(claim, any()) } just runs

        // When
        val result = defaultPermissionService.remove(claim, ClaimPermission.Build)

        // Then
        assertEquals(DefaultPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.remove(claim, any()) }
    }

    @Test
    fun `remove - when permission is linked to claim - returns UNCHANGED`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.remove(claim, any()) } just runs

        // When
        val result = defaultPermissionService.remove(claim, ClaimPermission.SignEdit)

        // Then
        assertEquals(DefaultPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.remove(claim, any()) }
    }

    @Test
    fun `removeAll - when no permissions are in repo - returns UNCHANGED`() {
        // Given
        every { permissionRepo.getByClaim(claim) } returns emptySet()
        every { permissionRepo.remove(claim, any()) } just runs

        // When
        val result = defaultPermissionService.removeAll(claim)

        // Then
        assertEquals(DefaultPermissionChangeResult.UNCHANGED, result)
        verify(exactly = 0) { permissionRepo.remove(claim, any()) }
    }

    @Test
    fun `removeAll - when some permissions are in repo - returns SUCCESS`() {
        // Given
        val permissions = setOf(ClaimPermission.SignEdit, ClaimPermission.DoorOpen, ClaimPermission.ContainerInspect)
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.remove(claim, any()) } just runs

        // When
        val result = defaultPermissionService.removeAll(claim)

        // Then
        assertEquals(DefaultPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.remove(claim, any()) }
    }

    @Test
    fun `removeAll - when all permissions are in repo - returns SUCCESS`() {
        // Given
        val permissions = ClaimPermission.entries.toSet()
        every { permissionRepo.getByClaim(claim) } returns permissions
        every { permissionRepo.remove(claim, any()) } just runs

        // When
        val result = defaultPermissionService.removeAll(claim)

        // Then
        assertEquals(DefaultPermissionChangeResult.SUCCESS, result)
        verify { permissionRepo.remove(claim, any()) }
    }
}