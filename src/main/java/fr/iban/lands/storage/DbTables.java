package fr.iban.lands.storage;

import fr.iban.common.data.sql.DbAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbTables {

    public static void createTables() {
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_lands(
                              id UUID PRIMARY KEY NOT NULL DEFAULT (UUID()),
                              name VARCHAR(255),
                              type VARCHAR(255) NOT NULL,
                              owner_id UUID DEFAULT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                          );
                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_bans(
                              land_id UUID NOT NULL,
                              uuid UUID NOT NULL,
                              PRIMARY KEY (land_id, uuid),
                              FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                          );
                        """);
        createTable(
                """
                        CREATE TABLE IF NOT EXISTS land_trusts(
                            land_id UUID NOT NULL,
                            uuid VARCHAR(36) NOT NULL,
                            permission VARCHAR(255) NOT NULL,
                            PRIMARY KEY (land_id, uuid, permission),
                            FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                        );
                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_flags(
                              land_id UUID NOT NULL,
                              flag VARCHAR(255) NOT NULL,
                              PRIMARY KEY (land_id, flag),
                              FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                          );
                        """);
        createTable(
                """
                         CREATE TABLE IF NOT EXISTS land_links(
                             land_id UUID NOT NULL,
                             linked_land_id UUID NOT NULL,
                             link_type VARCHAR(255) NOT NULL,
                             PRIMARY KEY (land_id, linked_land_id, link_type),
                             FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE,
                             FOREIGN KEY (linked_land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                         );
                        """);
        createTable(
                """
                        CREATE TABLE IF NOT EXISTS land_chunks(
                            `server` VARCHAR(255) NOT NULL,
                            `world` VARCHAR(255) NOT NULL,
                            x INT NOT NULL,
                            z INT NOT NULL,
                            land_id UUID NOT NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`server`, `world`, x, z),
                            FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                        );

                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_world_default_land_lands(
                              `server` VARCHAR(255) NOT NULL,
                              `world` VARCHAR(255) NOT NULL,
                              land_id UUID NOT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`server`, `world`),
                              FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                          );
                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_sublands(
                              id UUID PRIMARY KEY NOT NULL DEFAULT (UUID()),
                              land_id UUID NOT NULL,
                              `server` VARCHAR(255),
                              `world` VARCHAR(255),
                              x1 INT,
                              y1 INT,
                              z1 INT,
                              x2 INT,
                              y2 INT,
                              z2 INT,
                              FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE,
                              FOREIGN KEY (id) REFERENCES land_lands(id) ON DELETE CASCADE
                          );
                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_limits(
                              uuid UUID NOT NULL,
                              chunk_limit INT UNSIGNED NOT NULL,
                              PRIMARY KEY (uuid)
                          );
                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_effects(
                              land_id UUID NOT NULL,
                              effect VARCHAR(255) NOT NULL,
                              amplifier INT NOT NULL,
                              PRIMARY KEY (land_id, effect),
                              FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                          );
                        """);
        createTable(
                """
                          CREATE TABLE IF NOT EXISTS land_commands(
                              id UUID PRIMARY KEY NOT NULL DEFAULT (UUID()),
                              land_id UUID NOT NULL,
                              command VARCHAR(255) NOT NULL,
                              as_console BOOLEAN NOT NULL,
                              FOREIGN KEY (land_id) REFERENCES land_lands(id) ON DELETE CASCADE
                          );
                        """);
    }

    private static void createTable(String statement) {
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
