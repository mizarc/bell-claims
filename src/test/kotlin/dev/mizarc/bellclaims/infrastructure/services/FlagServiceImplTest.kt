package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.FlagService
import dev.mizarc.bellclaims.application.enums.FlagChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.values.Flag
import dev.mizarc.bellclaims.domain.values.Position3D
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class FlagServiceImplTest {
    private lateinit var flagRepo: ClaimFlagRepository
    private lateinit var flagService: FlagService

    private lateinit var claim: Claim

    @BeforeEach
    fun setup() {
        flagRepo = mockk()
        flagService = FlagServiceImpl(flagRepo)

        claim = Claim(UUID.randomUUID(), mockk<OfflinePlayer>(), Position3D(15,85,10), "Test")
    }

    @Test
    fun `doesClaimHaveFlag - returns true if flag in claim`() {
        // Given
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags

        // When
        val result1 = flagService.doesClaimHaveFlag(claim, Flag.Explosions)
        val result2 = flagService.doesClaimHaveFlag(claim, Flag.MobGriefing)

        // Then
        assertTrue(result1,
            "Claim contains Explosions flag.")
        assertFalse(result2,
            "Claim does not contain MobGriefing flag.")
    }

    @Test
    fun `getByClaim - returns expected flags`() {
        // Given
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags

        // When
        val result = flagService.getByClaim(claim)

        // Then
        assertEquals(setOf(Flag.Explosions, Flag.Pistons), result,
            "Claim contains Explosions and Pistons.")
        assertNotEquals(setOf(Flag.MobGriefing, Flag.FireSpread), result,
            "Claim does not contain MobGriefing or FireSpread.")
    }

    @Test
    fun `add - when flag is not in repo - returns SUCCESS`() {
        // Given
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns emptySet()
        every { flagRepo.add(claim, flag) } returns Unit

        // When
        val result = flagService.add(claim, flag)

        // Then
        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.add(claim, flag) }
    }

    @Test
    fun `add - when flag is in repo - returns UNCHANGED`() {
        // Given
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns setOf(flag)

        // When
        val result = flagService.add(claim, flag)

        // Then
        assertEquals(FlagChangeResult.UNCHANGED, result)
        verify(exactly = 0) { flagRepo.add(any(), any()) }
    }

    @Test
    fun `addAll - when no flags are in repo - returns SUCCESS`() {
        // Given
        every { flagRepo.getByClaim(claim) } returns emptySet()
        every { flagRepo.add(claim, any()) } returns Unit

        // When
        val result = flagService.addAll(claim)

        // Then
        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.add(claim, any()) }
    }

    @Test
    fun `addAll - when some flags are in repo - returns SUCCESS`() {
        // Given
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags
        every { flagRepo.add(claim, any()) } returns Unit

        // When
        val result = flagService.addAll(claim)

        // Then
        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.add(any(), any()) }
    }

    @Test
    fun `addAll - when all flags are in repo - returns UNCHANGED`() {
        // Given
        every { flagRepo.getByClaim(claim) } returns Flag.entries.toSet()

        // When
        val result = flagService.addAll(claim)

        // Then
        assertEquals(FlagChangeResult.UNCHANGED, result)
        verify(exactly = 0) { flagRepo.add(any(), any()) }
    }

    @Test
    fun `remove - when flag is in repo - returns SUCCESS`() {
        // Given
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns setOf(flag)
        every { flagRepo.remove(claim, flag) } returns Unit

        // When
        val result = flagService.remove(claim, flag)

        // Then
        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.remove(claim, flag) }
    }

    @Test
    fun `remove - when flag is not in repo - returns UNCHANGED`() {
        // Given
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns emptySet()

        // When
        val result = flagService.remove(claim, flag)

        // Then
        assertEquals(FlagChangeResult.UNCHANGED, result)
        verify(exactly = 0) { flagRepo.remove(any(), any()) }
    }

    @Test
    fun `removeAll - when all flags are in repo - returns SUCCESS`() {
        // Given
        every { flagRepo.getByClaim(claim) } returns Flag.entries.toSet()
        every { flagRepo.remove(claim, any()) } returns Unit

        // When
        val result = flagService.removeAll(claim)

        // Then
        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.remove(any(), any()) }
    }

    @Test
    fun `removeAll - when some flags are in repo - returns SUCCESS`() {
        // Given
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags
        every { flagRepo.remove(claim, any()) } returns Unit

        // When
        val result = flagService.removeAll(claim)

        // Then
        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.remove(any(), any()) }
    }

    @Test
    fun `removeAll - when no flags are in repo - returns UNCHANGED`() {
        // Given
        every { flagRepo.getByClaim(claim) } returns emptySet()

        // When
        val result = flagService.removeAll(claim)

        // Then
        assertEquals(FlagChangeResult.UNCHANGED, result)
        verify(exactly = 0) { flagRepo.remove(any(), any()) }
    }
}