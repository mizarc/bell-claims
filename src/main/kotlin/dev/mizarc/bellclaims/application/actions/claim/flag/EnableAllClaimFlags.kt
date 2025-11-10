package dev.mizarc.bellclaims.application.actions.claim.flag

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.flags.EnableAllClaimFlagsResult
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * Action for adding all flags to a claim, respecting configured blacklisted flags.
 */
class EnableAllClaimFlags(
    private val flagRepository: ClaimFlagRepository,
    private val claimRepository: ClaimRepository,
    private val config: MainConfig
) {

    /**
     * Adds all available non-blacklisted flags to the claim with the given [claimId].
     */
    fun execute(claimId: UUID): EnableAllClaimFlagsResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return EnableAllClaimFlagsResult.ClaimNotFound

        // Add all allowed flags to the claim
        var anyFlagEnabled = false
        try {
            val allFlags = Flag.entries
                .filter { flag -> !config.blacklistedFlags.any { it.equals(flag.name, ignoreCase = true) } }
            for (flag in allFlags) {
                if (flagRepository.add(claimId, flag)) anyFlagEnabled = true
            }

            // Return success if at least one flag was enabled
            return if (anyFlagEnabled) {
                EnableAllClaimFlagsResult.Success
            } else {
                EnableAllClaimFlagsResult.AllAlreadyEnabled
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.message}")
            return EnableAllClaimFlagsResult.StorageError
        }
    }
}