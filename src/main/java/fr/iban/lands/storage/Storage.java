package fr.iban.lands.storage;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.objects.*;
import fr.iban.lands.utils.Cuboid;
import fr.iban.lands.utils.DateUtils;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;

public class Storage implements AbstractStorage {

    private final DataSource ds = DbAccess.getDataSource();

    @Override
    public Map<SChunk, Integer> getChunks() {
        Map<SChunk, Integer> chunks = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM sc_chunks WHERE server=?;")) {
                ps.setString(1, LandsPlugin.getInstance().getServerName());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("idL");
                        String server = rs.getString("server");
                        String world = rs.getString("world");
                        Date createdAt = rs.getTimestamp("createdAt");
                        int x = rs.getInt("x");
                        int z = rs.getInt("z");
                        chunks.put(new SChunk(server, world, x, z, createdAt), id);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunks;
    }


    @Override
    public Map<Integer, Land> getLands() {
        Map<Integer, Land> lands = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT L.idL, L.libelleL, TL.libelleTL, L.uuid, L.lastPayment, L.createdAt " +
                            "FROM sc_lands L"
                            + " JOIN sc_land_types TL ON L.idTL=TL.idTL WHERE TL.libelleTL NOT LIKE 'SUBLAND';")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Land land = null;
                        int id = rs.getInt("idL");
                        LandType type = LandType.valueOf(rs.getString("libelleTL"));
                        String name = rs.getString("libelleL");
                        Date lastPayment = rs.getTimestamp("lastPayment");
                        Date createdAt = rs.getTimestamp("createdAt");
                        if (type == LandType.PLAYER) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            land = new PlayerLand(id, uuid, name);
                        } else if (type == LandType.GUILD) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            if(LandsPlugin.getInstance().isGuildsHookEnabled()) {
                                AbstractGuildDataAccess guildDataAccess = LandsPlugin.getInstance().getGuildDataAccess();
                                if (guildDataAccess.guildExists(uuid)) {
                                    land = new GuildLand(id, uuid, name);
                                } else continue;
                            }
                        } else {
                            land = new SystemLand(id, name);
                        }
                        if (land != null) {
                            land.setId(id);
                            land.setName(name);
                            land.setLastPayment(lastPayment);
                            land.setCreatedAt(createdAt);
                            loadTrusts(land);
                            land.setFlags(getFlags(land));
                            land.setBans(getBans(land));
                            land.setSubLands(getSubLands(land));
                            lands.put(land.getId(), land);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lands;
    }

    @Override
    public Land getLand(int id) {
        Land land = null;
        String sql = "SELECT L.idL, L.libelleL, TL.libelleTL, L.uuid, L.lastPayment " +
                "FROM sc_lands L" +
                " JOIN sc_land_types TL ON L.idTL=TL.idTL WHERE TL.libelleTL NOT LIKE 'SUBLAND' AND L.idL=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LandType type = LandType.valueOf(rs.getString("libelleTL"));
                        String name = rs.getString("libelleL");
                        Date lastPayment = rs.getTimestamp("lastPayment");
                        Date createdAt = rs.getTimestamp("createdAt");
                        if (type == LandType.PLAYER) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            land = new PlayerLand(id, uuid, name);
                        } else if (type == LandType.GUILD) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            if(LandsPlugin.getInstance().isGuildsHookEnabled()) {
                                AbstractGuildDataAccess guildDataAccess = LandsPlugin.getInstance().getGuildDataAccess();
                                if (guildDataAccess.guildExists(uuid)) {
                                    land = new GuildLand(id, uuid, name);
                                } else continue;
                            }
                        } else {
                            land = new SystemLand(id, name);
                        }
                        if (land != null) {
                            land.setId(id);
                            land.setName(name);
                            land.setLastPayment(lastPayment);
                            land.setCreatedAt(createdAt);
                            loadTrusts(land);
                            land.setFlags(getFlags(land));
                            land.setBans(getBans(land));
                            land.setSubLands(getSubLands(land));
                        }
                        return land;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return land;
    }

    @Override
    public void addLand(Land land) {
        String sql = "INSERT INTO sc_lands (libelleL, idTL, uuid) VALUES(?, (SELECT idTL FROM sc_land_types WHERE libelleTL LIKE ?), ?);";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getName());
                ps.setString(2, land.getType().toString());
                if (land.getOwner() != null) {
                    ps.setString(3, land.getOwner().toString());
                } else {
                    ps.setNull(3, Types.VARCHAR);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteLand(Land land) {
        String trustsSql = "DELETE FROM sc_trusts WHERE idL=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(trustsSql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String chunksSql = "DELETE FROM sc_chunks WHERE idL=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(chunksSql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String flagsSql = "DELETE FROM sc_flags WHERE idL=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(flagsSql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String bansSql = "DELETE FROM sc_lands_bans WHERE idL=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(bansSql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String linksSql = "DELETE FROM sc_lands_links WHERE idL=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(linksSql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String linksWithSql = "DELETE FROM sc_lands_links WHERE idLW=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(linksWithSql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "DELETE FROM sc_lands WHERE idL=?";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (land.getType() == LandType.SUBLAND) {
            deleteSubLandRegion((SubLand) land);
        }
    }

    @Override
    public int getLandID(LandType type, UUID uuid, String name) {
        String sql = "SELECT idL FROM sc_lands L JOIN sc_land_types TL ON TL.idTL=L.idTL WHERE TL.libelleTL=? AND L.uuid=? AND L.libelleL=? LIMIT 1;";
        int id = 0;
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, type.toString());
                ps.setString(2, uuid.toString());
                ps.setString(3, name);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        id = rs.getInt("idL");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public int getSystemLandID(String name) {
        String sql = "SELECT idL FROM sc_lands L JOIN sc_land_types TL ON TL.idTL=L.idTL WHERE TL.libelleTL=? AND L.libelleL=? LIMIT 1;";
        int id = 0;
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, LandType.SYSTEM.toString());
                ps.setString(2, name);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        id = rs.getInt("idL");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public void setChunk(Land land, SChunk chunk) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sc_chunks (server, world, x, z, idL) " +
                            "VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE idL=VALUES(idL);")) {
                ps.setString(1, chunk.getServer());
                ps.setString(2, chunk.getWorld());
                ps.setInt(3, chunk.getX());
                ps.setInt(4, chunk.getZ());
                ps.setInt(5, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeChunk(SChunk chunk) {
        String sql = "DELETE FROM sc_chunks WHERE server LIKE ? AND world LIKE ? AND x=? AND z=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, chunk.getServer());
                ps.setString(2, chunk.getWorld());
                ps.setInt(3, chunk.getX());
                ps.setInt(4, chunk.getZ());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadTrusts(Land land) {
        String sql = "SELECT T.uuid, LP.libelleLP FROM sc_trusts T JOIN sc_land_permissions LP ON T.idLP=LP.idLP WHERE T.idL=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, land.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Action action = Action.valueOf(rs.getString("libelleLP"));
                        String id = rs.getString("uuid");
                        if (id.equals("GLOBAL")) {
                            land.trust(action);
                        } else if (id.equals("GUILD_MEMBER")) {
                            land.trustGuild(action);
                        } else {
                            UUID uuid = UUID.fromString(id);
                            land.trust(uuid, action);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addTrust(Land land, UUID uuid, Action action) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sc_trusts (idL, uuid, idLP) VALUES("
                            + "?, "
                            + "?,"
                            + " (SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?));")) {
                ps.setInt(1, land.getId());
                ps.setString(2, uuid.toString());
                ps.setString(3, action.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTrust(Land land, UUID uuid, Action action) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM sc_trusts "
                            + "WHERE idL=? "
                            + "AND uuid=? "
                            + "AND idLP=(SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?);")) {
                ps.setInt(1, land.getId());
                ps.setString(2, uuid.toString());
                ps.setString(3, action.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addGlobalTrust(Land land, Action action) {
        addCustomTrust(land, action, "GLOBAL");
    }

    @Override
    public void removeGlobalTrust(Land land, Action action) {
        removeCustomTrust(land, action, "GLOBAL");
    }

    @Override
    public void addGuildTrust(Land land, Action action) {
        addCustomTrust(land, action, "GUILD_MEMBER");
    }

    @Override
    public void removeGuildTrust(Land land, Action action) {
        removeCustomTrust(land, action, "GUILD_MEMBER");
    }

    @Override
    public void addCustomTrust(Land land, Action action, String identifier) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sc_trusts (idL, uuid, idLP) VALUES("
                            + "?, "
                            + "'" + identifier + "', "
                            + " (SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?));")) {
                ps.setInt(1, land.getId());
                ps.setString(2, action.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCustomTrust(Land land, Action action, String identifier) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM sc_trusts "
                            + "WHERE idL=? "
                            + "AND uuid LIKE '" + identifier + "' "
                            + "AND idLP=(SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?);")) {
                ps.setInt(1, land.getId());
                ps.setString(2, action.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<SChunk, Integer> getChunks(UUID uuid) {
        String sql = "SELECT * FROM `sc_chunks` HAVING idL IN (SELECT DISTINCT idL FROM sc_lands WHERE uuid LIKE '?'";
        Map<SChunk, Integer> chunks = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("idL");
                        String server = rs.getString("server");
                        String world = rs.getString("world");
                        Date createdAt = rs.getTimestamp("createdAt");
                        int x = rs.getInt("x");
                        int z = rs.getInt("z");
                        chunks.put(new SChunk(server, world, x, z, createdAt), id);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    @Override
    public int getChunkCount(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM sc_chunks WHERE idL IN (SELECT DISTINCT idL FROM sc_lands WHERE uuid LIKE ?);";
        int count = 0;
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        count = rs.getInt("COUNT(*)");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Map<SChunk, Integer> getChunks(Land land) {
        String sql = "SELECT * FROM `sc_chunks` WHERE idL=?";
        Map<SChunk, Integer> chunks = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, land.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("idL");
                        String server = rs.getString("server");
                        String world = rs.getString("world");
                        int x = rs.getInt("x");
                        int z = rs.getInt("z");
                        chunks.put(new SChunk(server, world, x, z), id);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    @Override
    public Set<Flag> getFlags(Land land) {
        Set<Flag> flags = new HashSet<>();
        String sql = "SELECT LF.libelleTF FROM sc_flags F JOIN sc_land_flags LF ON F.idTF=LF.idTF WHERE F.idL=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, land.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            flags.add(Flag.valueOf(rs.getString("libelleTF")));
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flags;
    }

    @Override
    public void addFlag(Land land, Flag flag) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sc_flags (idL, idTF) VALUES("
                            + "?, "
                            + " (SELECT idTF FROM sc_land_flags WHERE libelleTF LIKE ?));")) {
                ps.setInt(1, land.getId());
                ps.setString(2, flag.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFlag(Land land, Flag flag) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM sc_flags "
                            + "WHERE idL=? "
                            + "AND idTF=(SELECT idTF FROM sc_land_flags WHERE libelleTF LIKE ?);")) {
                ps.setInt(1, land.getId());
                ps.setString(2, flag.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<UUID> getBans(Land land) {
        Set<UUID> bans = new HashSet<>();
        String sql = "SELECT uuid FROM sc_lands_bans WHERE idL=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, land.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bans.add(UUID.fromString(rs.getString("uuid")));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bans;
    }

    @Override
    public void addBan(Land land, UUID uuid) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sc_lands_bans VALUES(?,?);")) {
                ps.setInt(1, land.getId());
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBan(Land land, UUID uuid) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM sc_lands_bans "
                            + "WHERE idL=? "
                            + "AND uuid=?;")) {
                ps.setInt(1, land.getId());
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadLinks(LandManager manager) {
        String sql = "SELECT * FROM sc_lands_links;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idL = rs.getInt("idL");
                        int idLW = rs.getInt("idLW");

                        Land land = manager.getLands().get(idL);
                        Land landwith = manager.getLands().get(idLW);
                        Link link = Link.valueOf(rs.getString("LinkType"));

                        if (landwith != null && land != null) {
                            land.addLink(link, landwith);
                        } else if(land != null) {
                            removeLink(land, link);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addLink(Land land, Link link, Land with) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sc_lands_links VALUES(?,?,?);")) {
                ps.setInt(1, land.getId());
                ps.setInt(2, with.getId());
                ps.setString(3, link.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeLink(Land land, Link link) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM sc_lands_links "
                            + "WHERE idL=? "
                            + "AND LinkType LIKE ?;")) {
                ps.setInt(1, land.getId());
                ps.setString(2, link.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renameLand(Land land, String name) {
        String sql = "UPDATE sc_lands SET libelleL=? WHERE idL=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt(2, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLandLastPaymentDate(Land land) {
        String sql = "UPDATE sc_lands SET lastPayment=? WHERE idL=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(DateUtils.convertToLocalDateTime(land.getLastPayment())));
                ps.setInt(2, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Map<Integer, SubLand> getSubLands(Land land) {
        Map<Integer, SubLand> sublands = new HashMap<>();
        String sql = "SELECT * FROM sc_sublands SL JOIN sc_lands L ON SL.idSubLand=L.idL WHERE SL.idLand=?;";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, land.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idSL = rs.getInt("idSubLand");
                        String name = rs.getString("libelleL");
                        String server = rs.getString("server");
                        String world = rs.getString("world");

                        int x1 = rs.getInt("x1");
                        int y1 = rs.getInt("y1");
                        int z1 = rs.getInt("z1");

                        int x2 = rs.getInt("x2");
                        int y2 = rs.getInt("y2");
                        int z2 = rs.getInt("z2");

                        SubLand subland = new SubLand(land, idSL, name);
                        if (Bukkit.getWorld(world) != null) {
                            subland.setCuboid(new Cuboid(Objects.requireNonNull(Bukkit.getWorld(world)), x1, y1, z1, x2, y2, z2), server);
                        }
                        loadTrusts(subland);
                        subland.setFlags(getFlags(subland));
                        subland.setBans(getBans(subland));
                        sublands.put(idSL, subland);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sublands;
    }


    @Override
    public void setSubLandRegion(Land land, SubLand subland) {
        String sql = "INSERT INTO sc_sublands (idSubLand, idLand, server, world, x1, y1, z1, x2, y2, z2) VALUES("
                + "?, ?, ?, ?, ?, ?, ? ,? ,? ,?) ON DUPLICATE KEY UPDATE server=VALUES(server), world=VALUES(world),"
                + "x1=VALUES(x1), y1=VALUES(y1), z1=VALUES(z1), x2=VALUES(x2), y2=VALUES(y2), z2=VALUES(z2);";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, subland.getId());
                ps.setInt(2, land.getId());
                Cuboid cuboid = subland.getCuboid();
                ps.setString(3, subland.getServer());
                ps.setString(4, cuboid.getWorld().getName());
                ps.setInt(5, cuboid.getLowerX());
                ps.setInt(6, cuboid.getLowerY());
                ps.setInt(7, cuboid.getLowerZ());
                ps.setInt(8, cuboid.getUpperX());
                ps.setInt(9, cuboid.getUpperY());
                ps.setInt(10, cuboid.getUpperZ());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void deleteSubLandRegion(SubLand land) {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM sc_sublands "
                            + "WHERE idSubLand=?;")) {
                ps.setInt(1, land.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLastId(LandType type) {
        String sql = "SELECT max(idL) FROM sc_lands L JOIN sc_land_types TL ON TL.idTL=L.idTL WHERE TL.libelleTL=? LIMIT 1;";
        int id = 0;
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, type.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        id = rs.getInt("max(idL)");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


}
