package dev.mizarc.bellclaims.application.results

import dev.mizarc.bellclaims.domain.entities.Claim

sealed class CreateClaimResult {
    data class Success(val claim: Claim): CreateClaimResult()
    object NameCannotBeBlank: CreateClaimResult()
    object LimitExceeded: CreateClaimResult()
    object NameAlreadyExists: CreateClaimResult()
}