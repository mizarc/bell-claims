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

    override fun doesClaimHaveFlag(claimId: UUID, flag: Flag): Boolean {
        return flags[claimId]?.contains(flag) == true
    }

    override fun getByClaim(claimId: UUID): Set<Flag> {
        return flags[claimId]?.toSet() ?: setOf()
    }

    override fun add(claimId: UUID, flag: Flag): Boolean {
        // Write to cache
        flags.getOrPut(claimId) { mutableSetOf() }.add(flag)

        // Write to database
        try {
            val rowsAffected = storage.connection.executeUpdate("INSERT INTO claim_flags (claim_id, flag) " +
                    "VALUES (?,?) ON CONFLICT (claim_id, flag) DO NOTHING;", claimId, flag.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add flag '$flag' for claim_id '$claimId' to the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun remove(claimId: UUID, flag: Flag): Boolean {
        // Remove from cache
        val claimFlags = flags[claimId] ?: return false
        claimFlags.remove(flag)
        if (claimFlags.isEmpty()) flags.remove(claimId)

        // Remove from database
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claim_flags WHERE claim_id=? AND flag=?",
                claimId, flag.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove flag '$flag' for claim_id '$claimId' from the " +
                    "database. Cause: ${error.message}", error)
        }
    }

    override fun removeByClaim(claimId: UUID): Boolean {
        // Remove from cache
        flags.remove(claimId)

        // Remove from database
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claim_flags WHERE claim_id=?", claimId)
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
                "CREATE TABLE IF NOT EXISTS claim_flags (claim_id TEXT, flag TEXT, FOREIGN KEY (claim_id) " +
                        "REFERENCES claims(id), UNIQUE (claim_id, flag))"
            )
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to create 'claim_flags' table. Cause: ${error.message}", error)
        }
    }

    /**
     * Fetches all claim flags from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM claim_flags")
        for (result in results) {
            val claimId = UUID.fromString(result.getString("claim_id"))
            val flag = Flag.valueOf(result.getString("flag"))
            flags.getOrPut(claimId) { mutableSetOf() }.add(flag)
        }
    }
}