package xyz.mizarc.solidclaims

import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.Player
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList


class DatabaseStorage(var plugin: SolidClaims) {
    private lateinit var connection: Connection

    fun openConnection() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:sqlite:" + plugin.dataFolder.toString() + "/claims.db"
            )
            createClaimTable()
            createPlayerTable()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun closeConnection() {
        try {
            connection.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun getClaim(id: Int): Claim? {
        try {
            // Get specified claim
            val sqlQuery = "SELECT * FROM claims WHERE id=?"
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {

                // Get all players trusted in claim
                val sqlPlayerQuery = "SELECT * FROM players WHERE id=?"
                val playerStatement = connection.prepareStatement(sqlPlayerQuery)
                playerStatement.setInt(1, resultSet.getInt(1))
                val playerResultSet = statement.executeQuery()
                val players: ArrayList<Player> = ArrayList()
                while (playerResultSet.next()) {
                    players.add(Player(UUID.fromString(playerResultSet.getString(1))))
                }

                return Claim(
                    resultSet.getInt(1),
                    UUID.fromString(resultSet.getString(2)),
                    UUID.fromString(resultSet.getString(3)),
                    players,
                )
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    fun addClaim(world: UUID, owner: UUID) {

    }

    private fun createClaimTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claims (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "owner TEXT NOT NULL);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

}