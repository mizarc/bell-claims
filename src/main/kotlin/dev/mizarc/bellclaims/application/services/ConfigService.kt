package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.config.MainConfig

interface ConfigService {
    fun loadConfig(): MainConfig
}