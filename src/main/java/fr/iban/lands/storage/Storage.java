package fr.iban.lands.storage;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LinkType;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.Land;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Storage {

    Map<SChunk, UUID> getChunks();

    int getChunkCount(UUID uuid);

    Map<UUID, Land> getLands();

    Map<String, UUID> getWorldsDefaultLands();

    void setWorldDefaultLand(String world, Land land);

    Map<UUID, UUID> getSubLands();

    Land getLand(UUID landId);

    void addLand(Land land);

    void deleteLand(Land land);

    void updateLand(Land land);

    void setChunk(Land land, SChunk chunk);

    void removeChunk(SChunk chunk);

    void loadTrusts(Land land);

    void addTrust(Land land, UUID uuid, Action action);

    void removeTrust(Land land, UUID uuid, Action action);

    void addGlobalTrust(Land land, Action action);

    void removeGlobalTrust(Land land, Action action);

    void addGuildTrust(Land land, Action action);

    void removeGuildTrust(Land land, Action action);

    Set<Flag> getFlags(Land land);

    void addFlag(Land land, Flag flag);

    void removeFlag(Land land, Flag flag);

    Set<UUID> getBans(Land land);

    void addBan(Land land, UUID uuid);

    void removeBan(Land land, UUID uuid);

    Map<UUID, Map<LinkType, UUID>> getLinks();

    void addLink(Land land, LinkType link, Land with);

    void removeLink(Land land, LinkType link);

    int getChunkLimit(UUID uuid);

    void setChunkLimit(UUID uuid, int limit);

    void increaseChunkLimit(UUID uuid, int amount);

    void decreaseChunkLimit(UUID uuid, int amount);
}
