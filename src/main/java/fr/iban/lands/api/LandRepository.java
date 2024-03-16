package fr.iban.lands.api;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LinkType;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.Land;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface LandRepository {
    Optional<Land> getLandById(UUID id);

    void addLand(Land land);

    void updateLand(Land land);

    void deleteLand(Land land);

    void reloadLand(UUID id);

    /**
     * Get the land at the given chunk. Prefer using the SChunk version of this
     * method to avoid chunk loading.
     *
     * @param chunk the chunk
     * @return the land at the given chunk
     */
    Land getLandAt(Chunk chunk);

    Land getLandAt(SChunk chunk);

    @NotNull
    Land getLandAt(Location location);

    Land getWilderness();

    void addChunk(SChunk chunk, Land land);

    void removeChunk(SChunk chunk);

    Map<SChunk, Land> getChunks();

    Collection<SChunk> getChunks(Land land);

    List<Land> getLands();

    List<Land> getLands(UUID uuid);

    List<Land> getGuildLands(UUID uuid);

    List<Land> getSystemLands();

    List<Land> getSystemLands(String name);

    List<String> getManageableLandsNames(Player player);

    Land getManageableLandFromName(Player player, String name);

    boolean canManageLand(Player player, Land land);

    void addTrust(Land land, UUID uuid, Action action);

    void removeTrust(Land land, UUID uuid, Action action);

    void addGlobalTrust(Land land, Action action);

    void removeGlobalTrust(Land land, Action action);

    void addGuildTrust(Land land, Action action);

    void removeGuildTrust(Land land, Action action);

    int getChunkLimit(UUID uuid);

    int getChunkCount(UUID uuid);

    int getMaxChunkCount(UUID uuid);

    int getRemainingChunkCount(UUID uuid);

    void setChunkLimit(UUID from, int limit);

    void increaseChunkLimit(UUID to, int maxChunkCount);

    void decreaseChunkLimit(UUID uuid, int amount);

    void setDefaultWorldLand(World world, Land land);

    void addFlag(Land land, Flag flag);

    void removeFlag(Land land, Flag flag);

    void addLink(Land land, LinkType link, Land with);

    void removeLink(Land land, LinkType link);

    void addBan(Land land, UUID uuid);

    void removeBan(Land land, UUID uuid);
}
