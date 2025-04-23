package dev.mizarc.bellclaims.infrastructure.persistence.storage

import co.aikar.idb.Database
import co.aikar.idb.DatabaseOptions
import co.aikar.idb.PooledDatabaseOptions
import org.bukkit.plugin.Plugin
import java.io.File

class SQLiteStorage(dataFolder: File): Storage<Database> {
    override val connection: Database

    init {
        val options = DatabaseOptions.builder().sqlite("$dataFolder/claims.db").build()
        connection = PooledDatabaseOptions.builder().options(options).createHikariDatabase()
    }
}