package fr.iban.lands.utils;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class LandMap {

	private final LandManager manager;
	private final Map<UUID, Land> landMapSelection = new HashMap<>();


	public LandMap(LandManager manager) {
		this.manager = manager;
	}

	public void display(Player player, Land land) {
		landMapSelection.put(player.getUniqueId(), land);
		List<BaseComponent[]> components = get(player, 15, 5, land);
		for (BaseComponent[] baseComponents : components) {
			player.sendMessage(baseComponents);
		}
	}

	public List<BaseComponent[]> get(Player player, int rangeX, int rangeZ, Land land) {
		List<ComponentBuilder> components = new ArrayList<>();
		String servername = LandsPlugin.getInstance().getServerName();
		final Chunk center = player.getChunk();
		final World world = center.getWorld();

		final int startX = center.getX() - rangeX;
		final int startZ = center.getZ() - rangeZ;
		final int endX = center.getX() + rangeX+1;
		final int endZ = center.getZ() + rangeZ+1;

		for (int i = 0; i < rangeZ*2+1 ; i++) {
			components.add(new ComponentBuilder());
		}


		for (int x = startX; x < endX; x++) {

			int i = 0;
			for (int z = startZ; z < endZ; z++) {

				SChunk chunkXZ = new SChunk(servername, world.getName(), x, z);
				components.get(i).append(getSymbole(player, center, chunkXZ, land));
				i++;

			}

		}

		List<BaseComponent[]> baseComponents = new ArrayList<>();
		for (ComponentBuilder componentBuilder : components) {
			baseComponents.add(componentBuilder.create());
		}

		BaseComponent[] headerAndFooter = new ComponentBuilder(StringUtils.repeat("█", rangeX*2+1)).color(ChatColor.BLACK).create();

		baseComponents.add(0, headerAndFooter);

		return baseComponents;
	}


	@SuppressWarnings("deprecation")
	private BaseComponent[] getSymbole(Player player, Chunk center, SChunk schunk, Land selectedLand) {

		ComponentBuilder builder = new ComponentBuilder("█").color(HexColor.MARRON_CLAIR.getColor());

		//Hover
		ComponentBuilder hoverbase = new ComponentBuilder();
		ComponentBuilder clicToClaim = new ComponentBuilder("\n(clic pour claim)").color(ChatColor.GRAY);
		ComponentBuilder clicToUnclaim = new ComponentBuilder("\n(clic pour unclaim)").color(ChatColor.GRAY);


		Land land = manager.getLandAt(schunk);

		if(land instanceof SystemLand){
				SystemLand sland = (SystemLand)land;
				hoverbase.append(new ComponentBuilder(sland.getName()).bold(true).color(HexColor.MARRON.getColor()).create());
				if(land.getName().equals("Zone sauvage")) {
					if(selectedLand != null) {
						hoverbase.append(clicToClaim.create()).color(ChatColor.GRAY).create();
						builder.event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/land claimat " + schunk.getWorld() + " " + schunk.getX() + " " + schunk.getZ()));
					}
				}else {
					builder.color(HexColor.OLIVE.getColor());
					if(!sland.equals(selectedLand)) {
						builder.event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, ""));
					}else {
						builder.event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/land unclaimat " + schunk.getWorld() + " " + schunk.getX() + " " + schunk.getZ()));
					}
				}
		}else if(land instanceof PlayerLand) {
			PlayerLand pland = (PlayerLand)land;
			if(Objects.equals(pland.getOwner(), player.getUniqueId())) {
				builder.color(ChatColor.DARK_GREEN);
				hoverbase.append(new ComponentBuilder("Territoire : " + pland.getName() + "\n").color(ChatColor.DARK_GREEN).append(TextComponent.fromLegacyText("Propriétaire : Vous")).color(ChatColor.DARK_GREEN).create());
				if(selectedLand != null) {
					hoverbase.append(clicToUnclaim.create());
					builder.event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/land unclaimat " + schunk.getWorld() + " " + schunk.getX() + " " + schunk.getZ()));
				}
			}else {
				builder.color(HexColor.MARRON.getColor());
				hoverbase.append(new ComponentBuilder("Territoire : " + pland.getName() + "\n").color(HexColor.MARRON.getColor()).append(TextComponent.fromLegacyText("Propriétaire : " + Bukkit.getOfflinePlayer(pland.getOwner()).getName())).color(HexColor.MARRON.getColor()).create());
			}
		}else if(land instanceof GuildLand) {
			GuildLand guildLand = (GuildLand)land;
			if(guildLand.isGuildMember(player.getUniqueId())) {
				builder.color(ChatColor.AQUA);
				hoverbase.append(new ComponentBuilder("Territoire de guilde : " + guildLand.getName() + "\n").color(ChatColor.DARK_GREEN).append(TextComponent.fromLegacyText("Propriétaire : Guilde " + guildLand.getGuildName())).color(HexColor.MARRON.getColor()).color(ChatColor.DARK_GREEN).create());
				if(selectedLand != null) {
					hoverbase.append(clicToUnclaim.create());
					builder.event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/land unclaimat " + schunk.getWorld() + " " + schunk.getX() + " " + schunk.getZ()));
				}
			}else {
				builder.color(HexColor.MARRON.getColor());
				hoverbase.append(new ComponentBuilder("Territoire de guilde : " + guildLand.getName() + "\n").color(HexColor.MARRON.getColor()).append(TextComponent.fromLegacyText("Propriétaire : Guilde " + guildLand.getGuildName())).color(HexColor.MARRON.getColor()).create());
			}
		}
		if(center.getX() == schunk.getX() && center.getZ() == schunk.getZ()) {
			builder.color(ChatColor.YELLOW);
			builder.event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("VOUS").bold(true).color(ChatColor.YELLOW).create()));
		}else {
			builder.event(new HoverEvent(Action.SHOW_TEXT, hoverbase.create()));
		}

		return builder.create();
	}

	public Map<UUID, Land> getLandMapSelection() {
		return landMapSelection;
	}

}
