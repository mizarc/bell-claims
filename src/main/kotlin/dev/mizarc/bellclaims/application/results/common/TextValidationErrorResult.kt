package dev.mizarc.bellclaims.application.results.common

sealed class TextValidationErrorResult {
    data class ExceededCharacterLimit(val maxCharacters: String) : TextValidationErrorResult()
    data class InvalidCharacters(val invalidCharacters: String) : TextValidationErrorResult()
    data class ContainsBlacklistedWord(val blacklistedWord: String) : TextValidationErrorResult()
    object NoCharactersProvided : TextValidationErrorResult()
}