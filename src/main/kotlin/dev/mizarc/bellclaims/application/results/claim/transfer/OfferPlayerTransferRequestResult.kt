package dev.mizarc.bellclaims.application.results.claim.transfer

sealed class OfferPlayerTransferRequestResult {
    object Success: OfferPlayerTransferRequestResult()
    object ClaimNotFound: OfferPlayerTransferRequestResult()
    object RequestAlreadyPending: OfferPlayerTransferRequestResult()
    object StorageError: OfferPlayerTransferRequestResult()
}