package fr.iban.lands.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;

public class DbTables {
	
	private void createTable(String statement) {
		try (Connection connection = DbAccess.getDataSource().getConnection()) {
			try(PreparedStatement preparedStatemente = connection.prepareStatement(statement)){
				preparedStatemente.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertLandFlag(Flag flag) {
		String statement = "INSERT INTO sc_land_flags (libelleTF) VALUES (\""+flag.toString()+"\") ON DUPLICATE KEY UPDATE libelleTF=libelleTF;";
		
		try (Connection connection = DbAccess.getDataSource().getConnection()) {
			try(PreparedStatement preparedStatemente = connection.prepareStatement(statement)){
				preparedStatemente.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertLandAction(Action action) {
		String statement = "INSERT INTO sc_land_permissions (libelleLP) VALUES (\""+action.toString()+"\") ON DUPLICATE KEY UPDATE libelleLP=libelleLP;";
		
		try (Connection connection = DbAccess.getDataSource().getConnection()) {
			try(PreparedStatement preparedStatemente = connection.prepareStatement(statement)){
				preparedStatemente.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertLandType(LandType type) {
		String statement = "INSERT INTO sc_land_types (libelleTL) VALUES (\""+type.toString()+"\") ON DUPLICATE KEY UPDATE libelleTL=libelleTL;";
		try (Connection connection = DbAccess.getDataSource().getConnection()) {
			try(PreparedStatement preparedStatemente = connection.prepareStatement(statement)){
				preparedStatemente.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	public void create() {
		createTable("CREATE TABLE IF NOT EXISTS sc_land_permissions(" + 
				"    idLP INT NOT NULL AUTO_INCREMENT," + 
				"    libelleLP VARCHAR(255) NOT NULL UNIQUE," + 
				"    CONSTRAINT PK_land_permissions PRIMARY KEY (idLP)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_land_types(\n" + 
				"    idTL INT NOT NULL AUTO_INCREMENT,\n" + 
				"    libelleTL VARCHAR(255) NOT NULL UNIQUE,\n" + 
				"    CONSTRAINT PK_land_types PRIMARY KEY (idTL)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_land_flags(\n" + 
				"    idTF INT PRIMARY KEY AUTO_INCREMENT,\n" + 
				"    libelleTF VARCHAR(255) NOT NULL UNIQUE\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_lands(\n" + 
				"    idL INT NOT NULL AUTO_INCREMENT,\n" + 
				"    libelleL VARCHAR(255),\n" + 
				"    idTL INT NOT NULL,\n" +
				"    uuid VARCHAR(36) DEFAULT NULL, " +
				"    CONSTRAINT PK_lands PRIMARY KEY (idL),\n" + 
				"    CONSTRAINT FK_land_type FOREIGN KEY (idTL) REFERENCES sc_land_types(idTL)" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_lands_bans(\n" + 
				"    idL INT NOT NULL,\n" + 
				"    uuid VARCHAR(36) NOT NULL,\n" + 
				"    CONSTRAINT PK_lands_bans PRIMARY KEY (idL, uuid),\n" + 
				"    CONSTRAINT FK_lands_bans_land FOREIGN KEY (idL) REFERENCES sc_lands(idL)" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_trusts(\n" + 
				"    idL INT NOT NULL,\n" + 
				"    uuid VARCHAR(36) NOT NULL,\n" + 
				"    idLP INT NOT NULL,\n" + 
				"    CONSTRAINT PK_trusts PRIMARY KEY (idL, uuid, idLP),\n" + 
				"    CONSTRAINT FK_sc_trusts_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL),\n" + 
				"    CONSTRAINT FK_trusts_permissions FOREIGN KEY (idLP) REFERENCES sc_land_permissions(idLP)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_flags(\n" + 
				"    idL INT NOT NULL,\n" + 
				"    idTF INT NOT NULL,\n" + 
				"    CONSTRAINT PK_flags PRIMARY KEY (idL, idTF),\n" + 
				"    CONSTRAINT FK_sc_flags_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL),\n" + 
				"    CONSTRAINT FK_flags_types FOREIGN KEY (idTF) REFERENCES sc_land_flags(idTF)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_lands_links(\n" + 
				"    idL INT NOT NULL,\n" + 
				"    idLW INT NOT NULL,\n" +
				"    LinkType VARCHAR(255) NOT NULL,\n" + 
				"    CONSTRAINT PK_links PRIMARY KEY (idL, idLW, LinkType),\n" + 
				"    CONSTRAINT FK_sc_links_land FOREIGN KEY (idL) REFERENCES sc_lands(idL),\n" + 
				"    CONSTRAINT FK_sc_link_land2 FOREIGN KEY (idLW) REFERENCES sc_lands(idL)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_chunks(\n" + 
				"    `server` VARCHAR(255) NOT NULL,\n" + 
				"    `world` VARCHAR(255) NOT NULL,\n" + 
				"    x INT NOT NULL,\n" + 
				"    z INT NOT NULL,\n" + 
				"    idL INT NOT NULL,\n" + 
				"    CONSTRAINT PK_chunks PRIMARY KEY (`server`, world, x, z),\n" + 
				"    CONSTRAINT FK_chunks_lands FOREIGN KEY (idL) REFERENCES sc_lands(idL)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_sublands(\n" + 
				"    idSubLand INT NOT NULL,\n" + 
				"    idLand INT NOT NULL,\n" + 
				"    `server` VARCHAR(255),\n" + 
				"    `world` VARCHAR(255),\n" + 
				"    x1 INT ,\n" + 
				"    y1 INT ,\n" + 
				"    z1 INT ,\n" + 
				"    x2 INT ,\n" + 
				"    y2 INT ,\n" + 
				"    z2 INT ,\n" + 
				"    CONSTRAINT PK_sublands PRIMARY KEY (idSubland),\n" + 
				"    CONSTRAINT FK_sublands_lands FOREIGN KEY (idLand) REFERENCES sc_lands(idL)\n" + 
				");");
		for (Action action : Action.values()) {
			insertLandAction(action);
		}
		for (LandType type : LandType.values()) {
			insertLandType(type);
		}
		for(Flag flag : Flag.values()) {
			insertLandFlag(flag);
		}
	}

}
