package dev.mizarc.bellclaims.application.results.claim.transfer

import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult

sealed class AcceptTransferRequestResult {
    object Success : AcceptTransferRequestResult()
    object ClaimNotFound : AcceptTransferRequestResult()
    object NoActiveTransferRequest: AcceptTransferRequestResult()
    object ClaimLimitExceeded: AcceptTransferRequestResult()
    object BlockLimitExceeded: AcceptTransferRequestResult()
    object PlayerOwnsClaim: AcceptTransferRequestResult()
    object NameAlreadyExists: AcceptTransferRequestResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : AcceptTransferRequestResult()
    object StorageError : AcceptTransferRequestResult()
}
