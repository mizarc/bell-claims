package xyz.mizarc.solidclaims

import xyz.mizarc.solidclaims.claims.Claim
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class DatabaseStorage(var plugin: SolidClaims) {
    private lateinit var connection: Connection

    fun openConnection() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:sqlite:" + plugin.dataFolder.toString() + "/claims.db"
            )
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
        val sqlSelect = "SELECT * FROM claims WHERE id=?"

        try {
            val statement = connection.prepareStatement(sqlSelect)
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                return Claim(
                    resultSet.getInt(1),
                    resultSet.getString(2),
                    UUID.fromString(resultSet.getString(3)),
                    UUID.fromString(resultSet.getString(4)),
                )
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }
}