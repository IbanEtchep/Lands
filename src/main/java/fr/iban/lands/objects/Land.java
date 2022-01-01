package fr.iban.lands.objects;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import net.kyori.adventure.text.Component;

public abstract class Land {

	protected int id;
	protected String name;
	protected Trust globalTrust = new Trust();
	protected LandType type;
	protected Map<UUID, Trust> trusts = new HashMap<>();
	protected Set<Flag> flags = new HashSet<>();
	protected Set<UUID> bans = new HashSet<>();
	protected Map<Link, Land> links;
	protected Map<Integer, SubLand> subLands = new ConcurrentHashMap<>();
	
	public Land(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public abstract @Nullable UUID getOwner();
	
	public String getName() {
		if(name == null) {
			return "default";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LandType getType() {
		return type;
	}

	public void setType(LandType type) {
		this.type = type;
	}
	
	public boolean isWilderness() {
		return this instanceof SystemLand && getName().equals("Zone sauvage");
	}
	
	/*
	 * TRUSTS
	 */

	public Trust getGlobalTrust() {
		Land linkedLand = getLinkedLand(Link.GLOBALTRUST);
		if(linkedLand != null) {
			return linkedLand.getGlobalTrust();
		}
		return globalTrust;
	}

	public void setGlobalTrust(Trust globalTrust) {
		this.globalTrust = globalTrust;
	}

	public Map<UUID, Trust> getTrusts() {
		Land linkedLand = getLinkedLand(Link.TRUSTS);
		if(linkedLand != null) {
			return linkedLand.getTrusts();
		}
		return trusts;
	}

	public Trust getTrust(UUID uuid) {
		return getTrusts().getOrDefault(uuid, new Trust());
	}

	public void trust(UUID uuid, Action action) {
		if(!getTrusts().containsKey(uuid)) {
			getTrusts().put(uuid, new Trust());
		}
		getTrust(uuid).addPermission(action);
	}
	
	public void untrust(UUID uuid, Action action) {
		if(!getTrusts().containsKey(uuid)) {
			getTrusts().put(uuid, new Trust());
		}
		getTrust(uuid).removePermission(action);
		if(getTrust(uuid).getPermissions().isEmpty()) {
			getTrusts().remove(uuid);
		}
	}
	
	public void trust(Action action) {
		getGlobalTrust().addPermission(action);
	}
	
	public void untrust(Action action) {
		getGlobalTrust().removePermission(action);
	}
	
	public boolean isTrusted(UUID uuid, Action action) {
		return getTrusts().containsKey(uuid) && getTrust(uuid).hasPermission(action);
	}
	
	public boolean isBypassing(Player player, Action action) {
		UUID uuid = player.getUniqueId();
		boolean bypass = getGlobalTrust().hasPermission(action) || isTrusted(uuid, action) || LandsPlugin.getInstance().isBypassing(player);
		if((!bypass) && this instanceof PlayerLand) {
			player.sendActionBar(Component.text("§cVous n'avez pas la permission de faire cela dans ce claim."));
		}
		//Bukkit.broadcastMessage(action.toString() + " :" + bypass);
		return bypass;
	}

	public void setTrusts(Map<UUID, Trust> trusts) {
		this.trusts = trusts;
	}
	
	/*
	 * FLAGS
	 */
	
	public Set<Flag> getFlags() {
		return flags;
	}

	public void setFlags(Set<Flag> flags) {
		this.flags = flags;
	}
	
	public boolean hasFlag(Flag flag) {
		return getFlags().contains(flag);
	}

	public Set<UUID> getBans() {
		Land linkedLand = getLinkedLand(Link.BANS);
		if(linkedLand != null) {
			return linkedLand.getBans();
		}
		return bans;
	}

	public void setBans(Set<UUID> bans) {
		this.bans = bans;
	}
	
	public boolean isBanned(UUID uuid) {
		return getBans().contains(uuid);
	}

	public Map<Link, Land> getLinks() {
		if(links == null) {
			links = new EnumMap<>(Link.class);
		}
		return links;
	}

	public void addLink(Link link, Land with) {
		getLinks().put(link, with);
	}
	
	public void removeLink(Link link) {
		getLinks().remove(link);
	}
	
	public Land getLinkedLand(Link link) {
		Land land = getLinks().get(link);
		if(land == null) {
			removeLink(link);
		}
		return land;
	}
	
	public boolean hasSubLand() {
		return type != LandType.SUBLAND && getSubLands() != null && !getSubLands().isEmpty();
	}
	
	public SubLand getSubLandAt(Location loc) {
		if(hasSubLand()) {
			for(SubLand subland : subLands.values()) {
				if(subland.getCuboid() != null && subland.getServer() != null && subland.getServer().equals(CoreBukkitPlugin.getInstance().getServerName()) && subland.getCuboid().contains(loc)) {
					return subland;
				}
			}
		}
		return null;
	}
	
	public void setSubLands(Map<Integer, SubLand> subLands) {
		this.subLands = subLands;
	}
	
	public Map<Integer, SubLand> getSubLands() {
		return subLands;
	}
}
