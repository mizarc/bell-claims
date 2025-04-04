package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.domain.values.Flag
import java.sql.SQLException
import java.util.*

class ClaimFlagRepositorySQLite(private val storage: SQLiteStorage): ClaimFlagRepository {
    private val flags: MutableMap<UUID, MutableSet<Flag>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun doesClaimHaveFlag(claimId: UUID, rule: Flag): Boolean {
        return flags[claimId]?.contains(rule) == true
    }

    override fun getByClaim(claimId: UUID): Set<Flag> {
        return flags[claimId]?.toSet() ?: setOf()
    }

    override fun add(claimId: UUID, flag: Flag): Boolean {
        // Write to cache
        flags.getOrPut(claimId) { mutableSetOf() }.add(flag)

        // Write to database
        try {
            val rowsAffected = storage.connection.executeUpdate("INSERT NTO claimRules (claimId, rule) VALUES (?,?) " +
                    "ON CONFLICT (claimId, rule) DO NOTHING;", claimId, flag.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add flag '$flag' for claimId '$claimId' to the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun remove(claimId: UUID, flag: Flag): Boolean {
        // Remove from cache
        val claimRules = flags[claimId] ?: return false
        claimRules.remove(flag)
        if (claimRules.isEmpty()) flags.remove(claimId)

        // Remove from database
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claimRules WHERE claimId=? AND rule=?",
                claimId, flag.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove flag '$flag' for claimId '$claimId' from the " +
                    "database. Cause: ${error.message}", error)
        }
    }

    override fun removeByClaim(claimId: UUID): Boolean {
        // Remove from cache
        flags.remove(claimId)

        // Remove from database
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claimRules WHERE claimId=?", claimId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove all flags for claim $claimId from the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    /**
     * Creates a new table to store claim flag data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate(
                "CREATE TABLE IF NOT EXISTS claimRules (claimId TEXT, rule TEXT, FOREIGN KEY (claimId) " +
                        "REFERENCES claims(id), UNIQUE (claimId, rule))"
            )
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to create 'claimRules' table. Cause: ${error.message}", error)
        }
    }

    /**
     * Fetches all claim flags from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM claimRules")
        for (result in results) {
            val claimId = UUID.fromString(result.getString("claimId"))
            val rule = Flag.valueOf(result.getString("rule"))
            flags.getOrPut(claimId) { mutableSetOf() }.add(rule)
        }
    }
}