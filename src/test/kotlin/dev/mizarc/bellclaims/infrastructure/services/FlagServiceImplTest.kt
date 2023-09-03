package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.FlagService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.flags.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.flags.Flag
import dev.mizarc.bellclaims.domain.partitions.Position3D
import io.mockk.every
import io.mockk.mockk
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class FlagServiceImplTest {
    private lateinit var flagRepo: ClaimFlagRepository
    private lateinit var flagService: FlagService

    @BeforeEach
    fun setup() {
        flagRepo = mockk()
        flagService = FlagServiceImpl(flagRepo)
    }

    @Test
    fun doesClaimHaveFlag() {
        // Mock a claim that contains the flags "Explosions" and "Pistons"
        val claim = Claim(UUID.randomUUID(), mockk<OfflinePlayer>(), Position3D(15,85,10), "Test")
        val flags = setOf(Flag.Explosions, Flag.Pistons)
        every { flagRepo.getByClaim(claim) } returns flags

        assertTrue(flagService.doesClaimHaveFlag(claim, Flag.Explosions),
            "Claim contains Explosions flag.")
        assertFalse(flagService.doesClaimHaveFlag(claim, Flag.MobGriefing),
            "Claim does not contain MobGriefing flag.")
    }

    @Test
    fun getByClaim() {
    }

    @Test
    fun add() {
    }

    @Test
    fun addAll() {
    }

    @Test
    fun remove() {
    }

    @Test
    fun removeAll() {
    }
}