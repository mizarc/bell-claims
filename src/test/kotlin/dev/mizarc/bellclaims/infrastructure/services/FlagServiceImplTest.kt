package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.FlagService
import dev.mizarc.bellclaims.api.enums.FlagChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.flags.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.flags.Flag
import dev.mizarc.bellclaims.domain.partitions.Position3D
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

    private val claim = Claim(UUID.randomUUID(), mockk<OfflinePlayer>(), Position3D(15,85,10), "Test")

    @BeforeEach
    fun setup() {
        flagRepo = mockk()
        flagService = FlagServiceImpl(flagRepo)
    }

    @Test
    fun doesClaimHaveFlag() {
        // Mock a claim that contains the flags "Explosions" and "Pistons"
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags

        assertTrue(flagService.doesClaimHaveFlag(claim, Flag.Explosions),
            "Claim contains Explosions flag.")
        assertFalse(flagService.doesClaimHaveFlag(claim, Flag.MobGriefing),
            "Claim does not contain MobGriefing flag.")
    }

    @Test
    fun getByClaim() {
        // Mock a claim that contains the flags "Explosions" and "Pistons"
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags

        assertEquals(setOf(Flag.Explosions, Flag.Pistons), flagService.getByClaim(claim),
            "Claim contains Explosions and Pistons.")
        assertNotEquals(setOf(Flag.MobGriefing, Flag.FireSpread), flagService.getByClaim(claim),
            "Claim does not contain MobGriefing or FireSpread.")
    }

    @Test
    fun `add - when flag is not in repo - returns SUCCESS`() {
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns emptySet()
        every { flagRepo.add(claim, flag) } returns Unit

        assertEquals(FlagChangeResult.SUCCESS, flagService.add(claim, flag))
        verify { flagRepo.add(claim, flag) }
    }

    @Test
    fun `add - when flag is in repo - returns UNCHANGED`() {
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns setOf(flag)

        assertEquals(FlagChangeResult.UNCHANGED, flagService.add(claim, flag))
        verify(exactly = 0) { flagRepo.add(any(), any()) }
    }

    @Test
    fun `addAll - when no flags are in repo - returns SUCCESS`() {
        every { flagRepo.getByClaim(claim) } returns emptySet()
        every { flagRepo.add(claim, any()) } returns Unit

        val result = flagService.addAll(claim)

        assertEquals(FlagChangeResult.SUCCESS, result)
        verify { flagRepo.add(claim, any()) }
    }

    @Test
    fun `addAll - when some flags are in repo - returns SUCCESS`() {
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags
        every { flagRepo.add(claim, any()) } returns Unit

        assertEquals(FlagChangeResult.SUCCESS, flagService.addAll(claim))
        verify { flagRepo.add(any(), any()) }
    }

    @Test
    fun `addAll - when all flags are in repo - returns UNCHANGED`() {
        every { flagRepo.getByClaim(claim) } returns Flag.entries.toSet()

        val result = flagService.addAll(claim)

        assertEquals(FlagChangeResult.UNCHANGED, result)
        verify(exactly = 0) { flagRepo.add(any(), any()) }
    }

    @Test
    fun `remove - when flag is in repo - returns SUCCESS`() {
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns setOf(flag)
        every { flagRepo.remove(claim, flag) } returns Unit

        assertEquals(FlagChangeResult.SUCCESS, flagService.remove(claim, flag))
        verify { flagRepo.remove(claim, flag) }
    }

    @Test
    fun `remove - when flag is not in repo - returns UNCHANGED`() {
        val flag = Flag.Explosions
        every { flagRepo.getByClaim(claim) } returns emptySet()

        val result = flagService.remove(claim, flag)
        assertEquals(FlagChangeResult.UNCHANGED, result)
        verify(exactly = 0) { flagRepo.remove(any(), any()) }
    }

    @Test
    fun `removeAll - when all flags are in repo - returns SUCCESS`() {
        every { flagRepo.getByClaim(claim) } returns Flag.entries.toSet()
        every { flagRepo.remove(claim, any()) } returns Unit

        assertEquals(FlagChangeResult.SUCCESS, flagService.removeAll(claim))
        verify { flagRepo.remove(any(), any()) }
    }

    @Test
    fun `removeAll - when some flags are in repo - returns SUCCESS`() {
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags
        every { flagRepo.remove(claim, any()) } returns Unit

        assertEquals(FlagChangeResult.SUCCESS, flagService.removeAll(claim))
        verify { flagRepo.remove(any(), any()) }
    }

    @Test
    fun `removeAll - when no flags are in repo - returns UNCHANGED`() {
        every { flagRepo.getByClaim(claim) } returns emptySet()

        assertEquals(FlagChangeResult.UNCHANGED, flagService.removeAll(claim))
        verify(exactly = 0) { flagRepo.remove(any(), any()) }
    }
}