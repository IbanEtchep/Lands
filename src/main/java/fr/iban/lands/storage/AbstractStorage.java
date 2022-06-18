package fr.iban.lands.storage;

import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.SChunk;
import fr.iban.lands.objects.SubLand;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AbstractStorage {
		
	Map<SChunk, Integer> getChunks();
	
	Map<SChunk, Integer> getChunks(UUID uuid);
	
	Map<SChunk, Integer> getChunks(Land land);
	
	int getChunkCount(UUID uuid);
	
	Map<Integer, Land> getLands();

	Land getLand(int id);

	void addLand(Land land);
	
	void deleteLand(Land land);
	
	void renameLand(Land land, String name);
	
	int getLandID(LandType type, UUID uuid, String name);
	
	int getSystemLandID(String name);
		
	void setChunk(Land land, SChunk chunk);
	
	void removeChunk(SChunk chunk);
	
	void loadTrusts(Land land);

	void addTrust(Land land, UUID uuid, Action action);
	
	void removeTrust(Land land, UUID uuid, Action action);
	
	void addGlobalTrust(Land land, Action action);
	
	void removeGlobalTrust(Land land, Action action);

	void addGuildTrust(Land land, Action action);

	void removeGuildTrust(Land land, Action action);

	void addCustomTrust(Land land, Action action, String identifier);

	void removeCustomTrust(Land land, Action action, String identifier);

	Set<Flag> getFlags(Land land);
	
	void addFlag(Land land, Flag flag);
	
	void removeFlag(Land land, Flag flag);
	
	Set<UUID> getBans(Land land);
	
	void addBan(Land land, UUID uuid);
	
	void removeBan(Land land, UUID uuid);
	
	void loadLinks(LandManager manager);
	
	void addLink(Land land, Link link, Land with);
	
	void removeLink(Land land, Link link);
			
	Map<Integer, SubLand> getSubLands(Land land);

	void setSubLandRegion(Land land, SubLand subland);

	void deleteSubLandRegion(SubLand land);

	int getLastId(LandType type);

}
