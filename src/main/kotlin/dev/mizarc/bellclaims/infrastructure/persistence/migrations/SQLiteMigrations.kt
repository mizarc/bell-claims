package dev.mizarc.bellclaims.infrastructure.persistence.migrations

import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.SQLException

class SQLiteMigrations(private val plugin: JavaPlugin, private val connection: Connection) {
    fun migrate() {
        try {
            connection.autoCommit = false
            val currentDbVersion = getCurrentDatabaseVersion()
            plugin.logger.info(/* msg = */ "Current database schema version: v$currentDbVersion")

            // Migrate sequentially
            if (currentDbVersion < 2) {
                plugin.logger.info("Starting database migration from v$currentDbVersion to v${2}...")
                migrateToVersion2()
                updateDatabaseVersion(2)
            }
            // If you have more future migrations, add them here:
            // if (getCurrentDatabaseVersion() < 3) {
            //     migrateToVersion3()
            //     updateDatabaseVersion(3)
            //     plugin.logger.info("Migrated to database schema version 3.")
            // }
            connection.commit() // Commit transaction
        } catch (e: SQLException) {
            plugin.logger.severe("Database migration failed: ${e.message}")
            e.printStackTrace()
            try {
                connection.rollback() // Rollback on failure
                plugin.logger.warning("Database migration transaction rolled back.")
            } catch (rb: SQLException) {
                plugin.logger.severe("Failed to rollback database migration: ${rb.message}")
            }
            // You might want to disable the plugin here if migration is critical
            plugin.server.pluginManager.disablePlugin(plugin)
        } finally {
            connection.autoCommit = true // Reset auto-commit
        }
    }

    private fun getCurrentDatabaseVersion(): Int {
        connection.createStatement().use { stmt ->
            stmt.executeQuery("PRAGMA user_version;").use { rs ->
                val version = if (rs.next()) rs.getInt(1) else 0
                return version
            }
        }
    }

    private fun updateDatabaseVersion(version: Int) {
        connection.createStatement().use { stmt ->
            stmt.execute("PRAGMA user_version = $version;")
        }
    }

    /**
     * Migration from version 1 to version 2.
     * Contains all the provided SQL commands.
     */
    private fun migrateToVersion2() {
        plugin.logger.info("Starting migration to database v2.")
        val sqlCommands = mutableListOf<String>() // Use mutable list

        // --- Step 1: Foreign Keys OFF ---
        sqlCommands.add("PRAGMA foreign_keys = OFF;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear() // Clear list after execution

        // --- Step 2: Rename old tables ---
        sqlCommands.add("ALTER TABLE claimPartitions RENAME TO claim_partitions;")
        sqlCommands.add("ALTER TABLE claimPermissions RENAME TO claim_default_permissions;")
        sqlCommands.add("ALTER TABLE claimRules RENAME TO claim_flags;")
        sqlCommands.add("ALTER TABLE playerAccess RENAME TO claim_player_permissions;")
        sqlCommands.add("ALTER TABLE claims RENAME TO claims_old;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 3: claims table recreation and data migration ---
        sqlCommands.add("""
            CREATE TABLE claims (
                id TEXT PRIMARY KEY,
                world_id TEXT,
                owner_id TEXT,
                creation_time TEXT,
                name TEXT,
                description TEXT,
                position_x INTEGER,
                position_y INTEGER,
                position_z INTEGER,
                icon TEXT
            );
            """.trimIndent())
        sqlCommands.add("""
            INSERT INTO claims (id, world_id, owner_id, creation_time, name, description, position_x, position_y, position_z, icon)
            SELECT id, worldId, ownerId, creationTime, name, description, positionX, positionY, positionZ, icon FROM claims_old;
            """.trimIndent())
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 4: claim_default_permissions (Recreate, Insert, DROP OLD, RENAME NEW) ---
        sqlCommands.add("""
            CREATE TABLE claim_default_permissions_new (
                claim_id TEXT,
                permission TEXT,
                FOREIGN KEY (claim_id) REFERENCES claims(id),
                UNIQUE (claim_id, permission)
            );
            """.trimIndent())
        sqlCommands.add("INSERT INTO claim_default_permissions_new (claim_id, permission) SELECT claimId, permission FROM claim_default_permissions;")
        executeMigrationCommands(sqlCommands) // Execute these two, then ensure cursor is cleared
        sqlCommands.clear()

        sqlCommands.add("DROP TABLE claim_default_permissions;")
        sqlCommands.add("ALTER TABLE claim_default_permissions_new RENAME TO claim_default_permissions;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()


        // --- Step 5: claim_flags ---
        sqlCommands.add("""
            CREATE TABLE claim_flags_new (
                claim_id TEXT,
                flag TEXT,
                FOREIGN KEY (claim_id) REFERENCES claims(id),
                UNIQUE (claim_id, flag)
            );
            """.trimIndent())
        sqlCommands.add("INSERT INTO claim_flags_new (claim_id, flag) SELECT claimId, rule FROM claim_flags;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        sqlCommands.add("DROP TABLE claim_flags;")
        sqlCommands.add("ALTER TABLE claim_flags_new RENAME TO claim_flags;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 6: claim_partitions ---
        sqlCommands.add("""
            CREATE TABLE claim_partitions_new (
                id TEXT PRIMARY KEY,
                claim_id TEXT,
                lower_position_x INTEGER,
                lower_position_z INTEGER,
                upper_position_x INTEGER,
                upper_position_z INTEGER
            );
            """.trimIndent())
        sqlCommands.add("INSERT INTO claim_partitions_new (id, claim_id, lower_position_x, lower_position_z, upper_position_x, upper_position_z) SELECT id, claimId, lowerPositionX, lowerPositionZ, upperPositionX, upperPositionZ FROM claim_partitions;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        sqlCommands.add("DROP TABLE claim_partitions;")
        sqlCommands.add("ALTER TABLE claim_partitions_new RENAME TO claim_partitions;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 7: claim_player_permissions ---
        sqlCommands.add("""
            CREATE TABLE claim_player_permissions_new (
                claim_id TEXT,
                player_id TEXT,
                permission TEXT,
                FOREIGN KEY (claim_id) REFERENCES claims(id),
                UNIQUE (claim_id, player_id, permission)
            );
            """.trimIndent())
        sqlCommands.add("INSERT INTO claim_player_permissions_new (claim_id, player_id, permission) SELECT claimId, playerId, permission FROM claim_player_permissions;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        sqlCommands.add("DROP TABLE claim_player_permissions;")
        sqlCommands.add("ALTER TABLE claim_player_permissions_new RENAME TO claim_player_permissions;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 8: Update data in new tables ---
        sqlCommands.add("""
            UPDATE claim_flags
            SET flag = CASE
                WHEN flag = 'FireSpread' THEN 'FIRE'
                WHEN flag = 'MobGriefing' THEN 'MOB'
                WHEN flag = 'Explosions' THEN 'EXPLOSION'
                WHEN flag = 'Pistons' THEN 'PISTON'
                WHEN flag = 'Fluids' THEN 'FLUID'
                WHEN flag = 'Trees' THEN 'TREE'
                WHEN flag = 'Sculk' THEN 'SCULK'
                WHEN flag = 'Dispensers' THEN 'DISPENSER'
                WHEN flag = 'Sponge' THEN 'SPONGE'
                WHEN flag = 'Lightning' THEN 'LIGHTNING'
                WHEN flag = 'FallingBlock' THEN 'FALLING_BLOCK'
                ELSE flag
            END;
            """.trimIndent())
        sqlCommands.add("""
            UPDATE claim_default_permissions
            SET permission = CASE
                WHEN permission = 'Build' THEN 'BUILD'
                WHEN permission = 'ContainerInspect' THEN 'CONTAINER'
                WHEN permission = 'DisplayManipulate' THEN 'DISPLAY'
                WHEN permission = 'VehicleDeploy' THEN 'VEHICLE'
                WHEN permission = 'SignEdit' THEN 'SIGN'
                WHEN permission = 'RedstoneInteract' THEN 'REDSTONE'
                WHEN permission = 'DoorOpen' THEN 'DOOR'
                WHEN permission = 'VillagerTrade' THEN 'TRADE'
                WHEN permission = 'Husbandry' THEN 'HUSBANDRY'
                WHEN permission = 'Detonate' THEN 'DETONATE'
                WHEN permission = 'EventStart' THEN 'EVENT'
                WHEN permission = 'Sleep' THEN 'SLEEP'
                ELSE permission
            END;
            """.trimIndent())
        sqlCommands.add("""
            UPDATE claim_player_permissions
            SET permission = CASE
                WHEN permission = 'Build' THEN 'BUILD'
                WHEN permission = 'ContainerInspect' THEN 'CONTAINER'
                WHEN permission = 'DisplayManipulate' THEN 'DISPLAY'
                WHEN permission = 'VehicleDeploy' THEN 'VEHICLE'
                WHEN permission = 'SignEdit' THEN 'SIGN'
                WHEN permission = 'RedstoneInteract' THEN 'REDSTONE'
                WHEN permission = 'DoorOpen' THEN 'DOOR'
                WHEN permission = 'VillagerTrade' THEN 'TRADE'
                WHEN permission = 'Husbandry' THEN 'HUSBANDRY'
                WHEN permission = 'Detonate' THEN 'DETONATE'
                WHEN permission = 'EventStart' THEN 'EVENT'
                WHEN permission = 'Sleep' THEN 'SLEEP'
                ELSE permission
            END;
            """.trimIndent())
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 9: Drop old claims table ---
        sqlCommands.add("DROP TABLE claims_old;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        // --- Step 10: Foreign Keys ON ---
        sqlCommands.add("PRAGMA foreign_keys = ON;")
        executeMigrationCommands(sqlCommands)
        sqlCommands.clear()

        plugin.logger.info("Migration to database v2 applied successfully.")
    }

    /**
     * Helper function to execute a list of SQL commands sequentially.
     */
    private fun executeMigrationCommands(commands: List<String>) {
        commands.forEachIndexed { index, sql ->
            executeSql(sql)
        }
    }

    /**
     * Helper function to execute a single SQL command with error handling,
     * ensuring ResultSet/Statement are closed.
     */
    private fun executeSql(sql: String) {
        connection.createStatement().use { stmt ->
            try {
                val hasResultSet = stmt.execute(sql) // Execute and check if a ResultSet is produced
                if (hasResultSet) {
                    // If a ResultSet exists, consume and close it immediately.
                    // This is crucial for DDL following a SELECT or INSERT...SELECT.
                    stmt.resultSet?.close()
                }
            } catch (e: SQLException) {
                plugin.logger.severe("Failed to execute SQL: ${sql.substringBefore(';')}. Error: ${e.message}")
                throw e
            }
        }
    }
}