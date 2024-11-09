package fr.iban.lands.storage;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.LinkType;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.*;
import fr.iban.lands.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.logging.Logger;

public class SqlStorage implements Storage {

    private final DataSource ds = DbAccess.getDataSource();
    private final Logger logger;

    public SqlStorage(Logger logger) {
        DbTables.createTables();
        this.logger = logger;
    }

    @Override
    public Map<SChunk, UUID> getChunks() {
        Map<SChunk, UUID> chunks = new HashMap<>();
        String sql = "SELECT * FROM land_chunks WHERE server=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, LandsPlugin.getInstance().getServerName());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID landId = UUID.fromString(rs.getString("land_id"));
                        String server = rs.getString("server");
                        String world = rs.getString("world");
                        Date createdAt = rs.getTimestamp("created_at");
                        int x = rs.getInt("x");
                        int z = rs.getInt("z");

                        chunks.put(new SChunk(server, world, x, z, createdAt), landId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    @Override
    public Map<UUID, Land> getLands() {
        Map<UUID, Land> lands = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            String sql = "SELECT * FROM land_lands;";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Land land = getLandFromResultSet(rs);

                        if (land != null) {
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
    public Map<UUID, UUID> getSubLands() {
        Map<UUID, UUID> sublands = new HashMap<>();
        String sql = "SELECT id, land_id FROM land_sublands;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        UUID landId = UUID.fromString(rs.getString("land_id"));
                        sublands.put(id, landId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sublands;
    }

    @Override
    public Land getLand(UUID id) {
        String sql = "SELECT * FROM land_lands WHERE type NOT LIKE ? AND id=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, LandType.SUBLAND.toString());
                ps.setString(2, id.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return getLandFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Land getLandFromResultSet(ResultSet rs) throws SQLException {
        Land land = null;
        UUID id = UUID.fromString(rs.getString("id"));
        LandType type = LandType.valueOf(rs.getString("type"));
        String name = rs.getString("name");
        Date createdAt = rs.getTimestamp("created_at");

        switch (type) {
            case PLAYER -> {
                UUID ownerId = UUID.fromString(rs.getString("owner_id"));
                land = new PlayerLand(id, ownerId, name);
            }
            case GUILD -> {
                UUID ownerId = UUID.fromString(rs.getString("owner_id"));
                land = new GuildLand(id, ownerId, name);
            }
            case SYSTEM -> land = new SystemLand(id, name);
            case SUBLAND -> {
                land = new SubLand(id, name);
                loadSublandArea((SubLand) land);
            }
        }

        if (land != null) {
            land.setCreatedAt(createdAt);
            loadTrusts(land);
            land.setFlags(getFlags(land));
            land.setBans(getBans(land));
        }

        return land;
    }

    @Override
    public Map<String, UUID> getWorldsDefaultLands() {
        Map<String, UUID> worlds = new HashMap<>();
        String sql = "SELECT land_id, world FROM land_world_default_land_lands WHERE server=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, LandsPlugin.getInstance().getServerName());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("land_id"));
                        String world = rs.getString("world");
                        worlds.put(world, id);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return worlds;
    }

    @Override
    public void setWorldDefaultLand(String world, Land land) {
        String sql = "INSERT INTO land_world_default_land_lands (server, world, land_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE land_id=VALUES(land_id);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, LandsPlugin.getInstance().getServerName());
                ps.setString(2, world);
                ps.setString(3, land.getId().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addLand(Land land) {
        String sql = "INSERT INTO land_lands (id, name, type, owner_id) VALUES(?, ?, ?, ?);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, land.getName());
                ps.setString(3, land.getType().toString());

                if (land.getOwner() != null) {
                    ps.setString(4, land.getOwner().toString());
                } else {
                    ps.setNull(4, Types.NULL);
                }

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (land.getType() == LandType.SUBLAND) {
            updateSublandRegion((SubLand) land);
        }
    }

    @Override
    public void deleteLand(Land land) {
        String deleteLandSql = "DELETE FROM land_lands WHERE id=?";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(deleteLandSql)) {
                ps.setString(1, land.getId().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setChunk(Land land, SChunk chunk) {
        String sql = "INSERT INTO land_chunks (server, world, x, z, land_id) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE land_id=VALUES(land_id);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, chunk.getServer());
                ps.setString(2, chunk.getWorld());
                ps.setInt(3, chunk.getX());
                ps.setInt(4, chunk.getZ());
                ps.setString(5, land.getId().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeChunk(SChunk chunk) {
        String sql = "DELETE FROM land_chunks WHERE server LIKE ? AND world LIKE ? AND x=? AND z=?;";

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
        String sql = "SELECT uuid, permission FROM land_trusts WHERE land_id=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            Action action = Action.valueOf(rs.getString("permission"));
                            String id = rs.getString("uuid");

                            if (id.equals("GLOBAL")) {
                                land.trust(action);
                            } else if (id.equals("GUILD_MEMBER")) {
                                land.trustGuild(action);
                            } else {
                                UUID uuid = UUID.fromString(id);
                                land.trust(uuid, action);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warning("Permission " + rs.getString("permission") + " not found.");
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
        String sql = "INSERT INTO land_trusts (land_id, uuid, permission) VALUES(?, ?, ?);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
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
        String sql = "DELETE FROM land_trusts WHERE land_id=? AND uuid=? AND permission=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
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

    public void addCustomTrust(Land land, Action action, String identifier) {
        String sql = "INSERT INTO land_trusts (land_id, uuid, permission) VALUES(?, ?, ?);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, identifier);
                ps.setString(3, action.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeCustomTrust(Land land, Action action, String identifier) {
        String sql = "DELETE FROM land_trusts WHERE land_id=? AND uuid LIKE ? AND permission=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, identifier);
                ps.setString(3, action.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getChunkCount(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM land_chunks lc JOIN land_lands l ON lc.land_id = l.id WHERE l.owner_id LIKE ?;";

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
    public Set<Flag> getFlags(Land land) {
        Set<Flag> flags = new HashSet<>();
        String sql = "SELECT flag FROM land_flags WHERE land_id=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            flags.add(Flag.valueOf(rs.getString("flag")));
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
            String sql = "INSERT INTO land_flags (land_id, flag) VALUES(?, ?) ON DUPLICATE KEY UPDATE flag=VALUES(flag);";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, flag.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFlag(Land land, Flag flag) {
        String sql = "DELETE FROM land_flags WHERE land_id=? AND flag LIKE ?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
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
        String sql = "SELECT uuid FROM land_bans WHERE land_id=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
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
        String sql = "INSERT INTO land_bans VALUES(?, ?);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
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
            String sql = "DELETE FROM land_bans WHERE land_id=? AND uuid=?;";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, Map<LinkType, UUID>> getLinks() {
        Map<UUID, Map<LinkType, UUID>> links = new HashMap<>();
        String sql = "SELECT * FROM land_links;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID landId = UUID.fromString(rs.getString("land_id"));
                        UUID linkedLandId = UUID.fromString(rs.getString("linked_land_id"));
                        LinkType link = LinkType.valueOf(rs.getString("link_type"));

                        links.putIfAbsent(landId, new HashMap<>());
                        links.get(landId).put(link, linkedLandId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return links;
    }

    @Override
    public void addLink(Land land, LinkType link, Land with) {
        String sql = "INSERT INTO land_links VALUES(?, ?, ?);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, with.getId().toString());
                ps.setString(3, link.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeLink(Land land, LinkType link) {
        String sql = "DELETE FROM land_links WHERE land_id=? AND link_type LIKE ?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                ps.setString(2, link.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLand(Land land) {
        String sql = "UPDATE land_lands SET name=? WHERE id=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getName());
                ps.setString(2, land.getId().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (land instanceof SubLand subLand) {
            updateSublandRegion(subLand);
        }
    }

    public void loadSublandArea(SubLand land) {
        String sql = "SELECT * FROM land_sublands WHERE id=?;";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, land.getId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        String server = rs.getString("server");
                        String worldName = rs.getString("world");

                        int x1 = rs.getInt("x1");
                        int y1 = rs.getInt("y1");
                        int z1 = rs.getInt("z1");

                        int x2 = rs.getInt("x2");
                        int y2 = rs.getInt("y2");
                        int z2 = rs.getInt("z2");

                        World world = Bukkit.getWorld(worldName);
                        if (world != null) {
                            land.setCuboid(new Cuboid(world, x1, y1, z1, x2, y2, z2), server);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSublandRegion(SubLand subland) {
        String sql = "INSERT INTO land_sublands (id, land_id, server, world, x1, y1, z1, x2, y2, z2) VALUES("
                + "?, ?, ?, ?, ?, ?, ? ,? ,? ,?) ON DUPLICATE KEY UPDATE server=VALUES(server), world=VALUES(world),"
                + "x1=VALUES(x1), y1=VALUES(y1), z1=VALUES(z1), x2=VALUES(x2), y2=VALUES(y2), z2=VALUES(z2);";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, subland.getId().toString());
                ps.setString(2, subland.getSuperLand().getId().toString());
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
    public int getChunkLimit(UUID uuid) {
        String sql = "SELECT chunk_limit FROM land_limits WHERE uuid LIKE ?;";
        int maxClaims = 0;

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        maxClaims = rs.getInt("chunk_limit");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maxClaims;
    }

    @Override
    public void setChunkLimit(UUID uuid, int limit) {
        String sql = "INSERT INTO land_limits (uuid, chunk_limit) VALUES(?, ?) ON DUPLICATE KEY UPDATE chunk_limit=VALUES(chunk_limit);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, limit);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void increaseChunkLimit(UUID uuid, int amount) {
        String sql = "INSERT INTO land_limits (uuid, chunk_limit) VALUES(?, ?) ON DUPLICATE KEY UPDATE chunk_limit=chunk_limit+VALUES(chunk_limit);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, amount);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decreaseChunkLimit(UUID uuid, int amount) {
        String sql = "INSERT INTO land_limits (uuid, chunk_limit) VALUES(?, ?) ON DUPLICATE KEY UPDATE chunk_limit=chunk_limit-VALUES(chunk_limit);";

        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, amount);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
