package xyz.mizarc.solidclaims.claims

import xyz.mizarc.solidclaims.DatabaseStorage
import xyz.mizarc.solidclaims.Mapper
import xyz.mizarc.solidclaims.events.ClaimRule
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class ClaimRuleMapper(private val storage: DatabaseStorage) {
    init {
        createTable()
    }

    fun getByClaim(claimId: UUID): ArrayList<ClaimRule> {
        val claimRules = ArrayList<ClaimRule>()
        try {
            val results = storage.connection.getResults("SELECT rule FROM claimRules WHERE claimId=?;", claimId)
            for (result in results) {
                claimRules.add(ClaimRule.valueOf(result.getString("rule")))
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return claimRules
    }

    fun add(claimId: UUID, entity: ClaimRule) {
        try {
            storage.connection.executeUpdate("INSERT INTO claimRules VALUES (claimId, rule)" +
                    "VALUES (?,?,?)")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun remove(claimId: UUID, entity: PlayerAccess) {
        try {
            storage.connection.executeUpdate("REMOVE FROM claimRules WHERE claimId=? AND permissionId=?", entity)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store claim rule data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claimRules (claimId TEXT NOT NULL, " +
                    "rule TEXT NOT NULL, FOREIGN KEY (claimId) REFERENCES claims(id));")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}