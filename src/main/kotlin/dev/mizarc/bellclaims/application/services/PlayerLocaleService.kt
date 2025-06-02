package dev.mizarc.bellclaims.application.services

import java.util.*

interface PlayerLocaleService {
    fun getLocale(playerId: UUID): String
}