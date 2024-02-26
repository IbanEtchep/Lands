package fr.iban.lands.storage;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbTables {

    public static void createTables() {
        createTable(
                """
                                    CREATE TABLE IF NOT EXISTS sc_land_permissions(
                                        idLP INT NOT NULL AUTO_INCREMENT,
                                        libelleLP VARCHAR(255) NOT NULL UNIQUE,
                                        CONSTRAINT PK_land_permissions PRIMARY KEY (idLP)
                                    );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_land_types(
                               idTL INT NOT NULL AUTO_INCREMENT,
                               libelleTL VARCHAR(255) NOT NULL UNIQUE,
                               CONSTRAINT PK_land_types PRIMARY KEY (idTL)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_land_flags(
                               idTF INT PRIMARY KEY AUTO_INCREMENT,
                               libelleTF VARCHAR(255) NOT NULL UNIQUE
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_lands(
                               idL INT NOT NULL AUTO_INCREMENT,
                               libelleL VARCHAR(255),
                               idTL INT NOT NULL,
                               uuid VARCHAR(36) DEFAULT NULL,
                               lastPayment DATETIME DEFAULT NOW(),
                               createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT PK_lands PRIMARY KEY (idL),
                               CONSTRAINT FK_land_type FOREIGN KEY (idTL) REFERENCES sc_land_types(idTL)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_lands_bans(
                               idL INT NOT NULL,
                               uuid VARCHAR(36) NOT NULL,
                               CONSTRAINT PK_lands_bans PRIMARY KEY (idL, uuid),
                               CONSTRAINT FK_lands_bans_land FOREIGN KEY (idL) REFERENCES sc_lands(idL)
                               );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_trusts(
                               idL INT NOT NULL,
                               uuid VARCHAR(36) NOT NULL,
                               idLP INT NOT NULL,
                               CONSTRAINT PK_trusts PRIMARY KEY (idL, uuid, idLP),
                               CONSTRAINT FK_sc_trusts_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL),
                               CONSTRAINT FK_trusts_permissions FOREIGN KEY (idLP) REFERENCES sc_land_permissions(idLP)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_flags(
                               idL INT NOT NULL,
                               idTF INT NOT NULL,
                               CONSTRAINT PK_flags PRIMARY KEY (idL, idTF),
                               CONSTRAINT FK_sc_flags_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL),
                               CONSTRAINT FK_flags_types FOREIGN KEY (idTF) REFERENCES sc_land_flags(idTF)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_lands_links(
                               idL INT NOT NULL,
                               idLW INT NOT NULL,
                               LinkType VARCHAR(255) NOT NULL,
                               CONSTRAINT PK_links PRIMARY KEY (idL, idLW, LinkType),
                               CONSTRAINT FK_sc_links_land FOREIGN KEY (idL) REFERENCES sc_lands(idL),
                               CONSTRAINT FK_sc_link_land2 FOREIGN KEY (idLW) REFERENCES sc_lands(idL)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_chunks(
                               `server` VARCHAR(255) NOT NULL,
                               `world` VARCHAR(255) NOT NULL,
                               x INT NOT NULL,
                               z INT NOT NULL,
                               idL INT NOT NULL,
                               createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,    CONSTRAINT PK_chunks PRIMARY KEY (`server`, world, x, z),
                               CONSTRAINT FK_chunks_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_lands_world_default_lands(
                               `server` VARCHAR(255) NOT NULL,
                               `world` VARCHAR(255) NOT NULL,
                               idL INT NOT NULL,
                               createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,    CONSTRAINT PK_world_default_lands PRIMARY KEY (`server`, world),
                               CONSTRAINT FK_world_default_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_sublands(
                               idSubLand INT NOT NULL,
                               idLand INT NOT NULL,
                               `server` VARCHAR(255),
                               `world` VARCHAR(255),
                               x1 INT ,
                               y1 INT ,
                               z1 INT ,
                               x2 INT ,
                               y2 INT ,
                               z2 INT ,
                               CONSTRAINT PK_sublands PRIMARY KEY (idSubland),
                               CONSTRAINT FK_sublands_lands FOREIGN KEY (idLand) REFERENCES sc_lands(idL)
                           );
                        """);
        createTable(
                """
                           CREATE TABLE IF NOT EXISTS sc_lands_limits(
                                 uuid VARCHAR(36) NOT NULL,
                                 maxClaims INT NOT NULL,
                                 CONSTRAINT PK_lands_limits PRIMARY KEY (uuid)
                           );
                        """);
        for (Action action : Action.values()) {
            insertLandAction(action);
        }
        for (LandType type : LandType.values()) {
            insertLandType(type);
        }
        for (Flag flag : Flag.values()) {
            insertLandFlag(flag);
        }
    }

    private static void createTable(String statement) {
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatemente = connection.prepareStatement(statement)) {
                preparedStatemente.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertLandFlag(Flag flag) {
        String statement =
                "INSERT INTO sc_land_flags (libelleTF) VALUES (\""
                        + flag.toString()
                        + "\") ON DUPLICATE KEY UPDATE libelleTF=libelleTF;";

        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatemente = connection.prepareStatement(statement)) {
                preparedStatemente.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertLandAction(Action action) {
        String statement =
                "INSERT INTO sc_land_permissions (libelleLP) VALUES (\""
                        + action.toString()
                        + "\") ON DUPLICATE KEY UPDATE libelleLP=libelleLP;";

        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatemente = connection.prepareStatement(statement)) {
                preparedStatemente.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertLandType(LandType type) {
        String statement =
                "INSERT INTO sc_land_types (libelleTL) VALUES (\""
                        + type.toString()
                        + "\") ON DUPLICATE KEY UPDATE libelleTL=libelleTL;";
        try (Connection connection = DbAccess.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatemente = connection.prepareStatement(statement)) {
                preparedStatemente.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
