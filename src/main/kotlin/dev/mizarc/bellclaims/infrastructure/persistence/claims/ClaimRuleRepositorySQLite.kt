package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.claims.ClaimRuleRepository
import dev.mizarc.bellclaims.infrastructure.persistence.DatabaseStorage
import dev.mizarc.bellclaims.interaction.listeners.ClaimRule
import java.sql.SQLException
import java.util.*

class ClaimRuleRepositorySQLite(private val storage: DatabaseStorage): ClaimRuleRepository {
    private val rules: MutableMap<UUID, MutableSet<ClaimRule>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun doesClaimHaveRule(claim: Claim, rule: ClaimRule): Boolean {
        return rules[claim.id]?.contains(rule) ?: false
    }

    override fun getByClaim(claim: Claim): MutableSet<ClaimRule> {
        return rules[claim.id] ?: mutableSetOf()
    }

    override fun add(claim: Claim, rule: ClaimRule) {
        rules.getOrPut(claim.id) { mutableSetOf() }.add(rule)
        try {
            storage.connection.executeUpdate("INSERT INTO claimRules (claimId, rule)" +
                    "VALUES (?,?)", claim.id, rule.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(claim: Claim, rule: ClaimRule) {
        val claimRules = rules[claim.id] ?: return
        claimRules.remove(rule)
        if (claimRules.isEmpty()) {
            rules.remove(claim.id)
        }

        try {
            storage.connection.executeUpdate("DELETE FROM claimRules WHERE claimId=? AND rule=?",
                claim.id, rule.name)
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
            val rule = ClaimRule.valueOf(result.getString("rule"))
            rules.getOrPut(UUID.fromString(result.getString("claimId"))) { mutableSetOf() }.add(rule)
        }
    }
}