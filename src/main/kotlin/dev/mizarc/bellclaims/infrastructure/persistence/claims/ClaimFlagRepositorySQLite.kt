package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.domain.flags.Flag
import java.sql.SQLException
import java.util.*

class ClaimFlagRepositorySQLite(private val storage: SQLiteStorage): ClaimFlagRepository {
    private val rules: MutableMap<UUID, MutableSet<Flag>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun doesClaimHaveRule(claim: Claim, rule: Flag): Boolean {
        return rules[claim.id]?.contains(rule) ?: false
    }

    override fun getByClaim(claim: Claim): Set<Flag> {
        return rules[claim.id]?.toSet() ?: mutableSetOf()
    }

    override fun add(claim: Claim, flag: Flag) {
        rules.getOrPut(claim.id) { mutableSetOf() }.add(flag)
        try {
            storage.connection.executeUpdate("INSERT INTO claimRules (claimId, rule) VALUES (?,?)",
                claim.id, flag.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(claim: Claim, flag: Flag) {
        val claimRules = rules[claim.id] ?: return
        claimRules.remove(flag)
        if (claimRules.isEmpty()) {
            rules.remove(claim.id)
        }

        try {
            storage.connection.executeUpdate("DELETE FROM claimRules WHERE claimId=? AND rule=?",
                claim.id, flag.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun removeByClaim(claim: Claim){
        rules.remove(claim.id)

        try {
            storage.connection.executeUpdate("DELETE FROM claimRules WHERE claimId=?", claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claimRules (claimId TEXT, rule TEXT, " +
                    "FOREIGN KEY(claimId) REFERENCES claims(id));")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all player access permissions from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM claimRules")
        for (result in results) {
            val rule = Flag.valueOf(result.getString("rule"))
            rules.getOrPut(UUID.fromString(result.getString("claimId"))) { mutableSetOf() }.add(rule)
        }
    }
}