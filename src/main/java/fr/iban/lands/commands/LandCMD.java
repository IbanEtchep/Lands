package fr.iban.lands.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.objects.SChunk;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.bukkitcore.utils.HexColor;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.PlayerLand;
import fr.iban.lands.objects.SystemLand;
import fr.iban.lands.utils.ChatUtils;
import fr.iban.lands.utils.LandMap;
import fr.iban.lands.utils.SeeChunks;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class LandCMD implements CommandExecutor, TabCompleter {

	private LandManager landManager;
	private LandsPlugin plugin;

	private Map<UUID, SeeChunks> seeChunks = new HashMap<>();

	public LandCMD(LandsPlugin plugin) {
		this.plugin = plugin;
		this.landManager = plugin.getLandManager();
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(!(sender instanceof Player)) {
			sender.sendMessage("§cVous devez être joueur pour éxécuter cette commande !");
			return false;
		}

		Player player = (Player)sender;
		UUID uuid = player.getUniqueId();

		if(!plugin.getConfig().getBoolean("players-lands-enabled") && !sender.hasPermission("lands.bypass")) {
			sender.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
			return false;
		}

		if(args.length == 0) {
			player.performCommand("land help");
			return false;
		}

		switch (args[0].toLowerCase()) {
		case "claim":
			if(args.length == 1) {
				landManager.getPlayerFirstLand(player).thenAccept(land -> landManager.claim(player, new SChunk(player.getLocation().getChunk()), land, true));
			}else if(args.length == 2) {
				String landName = args[1];
				landManager.getPlayerLand(player, landName).thenAccept(land -> landManager.claim(player, new SChunk(player.getLocation().getChunk()), land, true));
			}
			break;
		case "unclaim":
			if(args.length == 1) {
				landManager.unclaim(player, new SChunk(player.getLocation().getChunk()), true);
			}else if(args.length == 2) {
				String landName = args[1];
				landManager.getPlayerLand(player, landName).thenAccept(land -> landManager.unclaim(player, new SChunk(player.getLocation().getChunk()), land, true));
			}
			break;
		case "forceunclaim":
			if(args.length == 1 && player.hasPermission("lands.admin")) {
				landManager.unclaim(player.getLocation().getChunk());
				player.sendMessage("§aLe claim a été enlevé.");
			}
			break;
		case "kick":
			if(args.length == 2) {
				Player target = Bukkit.getPlayer(args[1]);
				if(target != null ) {
					if(target.getUniqueId() != player.getUniqueId()) {
						Land land = landManager.getLandAt(target.getLocation());
						if(land instanceof PlayerLand) {
							PlayerLand pland = (PlayerLand)land;
							if(pland.getOwner().equals(player.getUniqueId())) {
								target.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
								target.sendMessage("§cVous avez été expulsé du territoire de " + player.getName());
								player.sendActionBar(Component.text("§aLe joueur a bien été expulsé."));
							}else {
								player.sendMessage("§cLe joueur n'est pas dans votre territoire !");
							}
						}
					}else {
						player.sendMessage("§cImpossible de faire cela sur vous même...");
					}
				}else {
					player.sendMessage("§cCe joueur n'est pas en ligne !");
				}
			}
			break;
		case "bypass":
			if(player.hasPermission("lands.bypass")) {
				if(plugin.getBypass().contains(player.getUniqueId())) {
					plugin.getBypass().remove(player.getUniqueId());
				}else {
					plugin.getBypass().add(player.getUniqueId());
				}
				player.sendMessage("§8§lBypass : " + (plugin.isBypassing(player) ? "§aActivé" : "§cDésactivé"));
			}
			break;
		case "create":
			if(args.length == 1) {
				player.sendMessage("/land create <NomDeLaRegion>");
			}else if(args.length == 2) {
				landManager.createLand(player, args[1]);
			}
			break;
		case "claimat":
			if(args.length == 4) {
				landManager.future(() -> {
					LandMap map = landManager.getLandMap();
					if(!map.getLandMapSelection().isEmpty() && map.getLandMapSelection().containsKey(uuid)) {
						Land land = map.getLandMapSelection().get(player.getUniqueId());
						if(land != null) {
							World world = Bukkit.getWorld(args[1]);
							int X = Integer.parseInt(args[2]);
							int Z = Integer.parseInt(args[3]);
							landManager.claim(player, new SChunk(CoreBukkitPlugin.getInstance().getServerName(), world.getName(), X, Z), land, true).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
								map.display(player, land);
							}));
						}
					}
				});
			}
			break;
		case "unclaimat":
			if(args.length == 4) {
				landManager.future(() -> {
					LandMap map = landManager.getLandMap();
					if(!map.getLandMapSelection().isEmpty() && map.getLandMapSelection().containsKey(uuid)) {
						Land land = map.getLandMapSelection().get(player.getUniqueId());
						if(land != null) {
							World world = Bukkit.getWorld(args[1]);
							int X = Integer.parseInt(args[2]);
							int Z = Integer.parseInt(args[3]);
							if(land instanceof PlayerLand) {
								landManager.unclaim(player, new SChunk(CoreBukkitPlugin.getInstance().getServerName(), world.getName(), X, Z), land, true);
							}else if(land instanceof SystemLand && player.hasPermission("lands.admin")) {
								landManager.unclaim(world.getChunkAt(X,Z));
							}
							player.sendActionBar("§a§lLe tronçon a bien été unclaim.");
							landManager.unclaim(player, new SChunk(CoreBukkitPlugin.getInstance().getServerName(), world.getName(), X, Z), land, true).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
								map.display(player, land);
							}));
						}
					}
				});
			}
			break;
		case "map":
			landManager.future(() -> {
				if(args.length == 1) {
					landManager.getLandMap().display(player, null);
				}else if(args.length == 2) {
					String landName = args[1];
					landManager.getPlayerLand(player, landName).thenAccept(land -> landManager.getLandMap().display(player, land));
				}
			});
			break;
		case "seeclaims":
			if(!seeChunks.containsKey(uuid)) {
				SeeChunks sc = new SeeChunks(player, landManager);
				seeChunks.put(player.getUniqueId(), sc);
				sc.showParticles();
				player.sendMessage("§aMode vision de claims activé.");
			}else {
				SeeChunks sc = seeChunks.get(uuid);
				sc.stop();
				seeChunks.remove(uuid);
				player.sendMessage("§cMode vision de claims désactivé.");
			}
			break;
		case "menu":
			player.performCommand("lands");
			break;
			//		case "migrate":
			//			if(player.hasPermission("lands.admin")) {
			//				ClaimsPlugin.getInstance().getClaimManager().getPlayersClaims().forEach((uid, claim) -> {
			//					landManager.createLand(uid, "default").thenAcceptAsync(land -> {
			//						for(ChunkXZ chunkxz : claim.getChunks()) {
			//							landManager.claim(new SChunk(CoreBukkitPlugin.getInstance().getServerName(), chunkxz.getWorld(), chunkxz.getX(), chunkxz.getZ()), land);
			//						}
			//						for(Entry<UUID, ClaimPerms> perms : claim.getPlayersPerms().entrySet()) {
			//							for(ClaimAction caction : perms.getValue().getPermissions()) {
			//								landManager.addTrust(land, perms.getKey(), Action.valueOf(caction.toString()));
			//							}
			//						}
			//						for(ClaimAction caction : claim.getAllsPerm().getPermissions()) {
			//							landManager.addGlobalTrust(land, Action.valueOf(caction.toString()));
			//						}
			//					});
			//				});
			//			}
			//			break;
		case "help":
			player.sendMessage(HexColor.MARRON_CLAIR.getColor() + "La protection de vos territoires se gère avec les commandes ci-dessous.");
			player.sendMessage("");
			player.sendMessage(getCommandUsage("/lands", "Ouvre le menu de gestion de vos territoires."));
			player.sendMessage(getCommandUsage("/land claim <territoire(optionnel)>", "Attribue le tronçon où vous vous trouvez au territoire choisi ou à votre premier territoire."));
			player.sendMessage(getCommandUsage("/land unclaim", "Retire le tronçon où vous vous trouvez de vos territoires."));
			player.sendMessage(getCommandUsage("/land map", "Affiche une carte des territoires alentours."));
			player.sendMessage(getCommandUsage("/land kick <joueur> ", "Renvois un joueur qui se trouve dans votre territoire au spawn."));
			player.sendMessage(getCommandUsage("/land seeclaims ", "Permet de voir les bordures des claims."));
			player.sendMessage("");
			player.sendMessage(HexColor.MARRON_CLAIR.getColor() + "Les " + HexColor.MARRON.getColor() + "tronçons(chunks) " + HexColor.MARRON_CLAIR.getColor() + "mesurent "
					+ HexColor.MARRON.getColor() +"16x256x16 blocs" + HexColor.MARRON_CLAIR.getColor() + " et sont visible en appuyant sur les touche "
					+ HexColor.MARRON.getColor() + "F3+G" + HexColor.MARRON_CLAIR.getColor()+".");
			break;
		default:
			break;
		}




		return false;
	}

	/*
	 * COMMANDES LAND
	 * 
	 * Alias : 
	 * 
	 * - /land create <NomRegion>
	 * - /land delete <NomRegion>
	 * - /land globaltrust <NomRegion(optionnel , default si absent)>
	 * - /land trust Joueur <NomRegion(optionnel , default si absent)>
	 * - /land trustlist
	 * - /land map
	 * - /land ban
	 * - /land kick
	 * - /land claim here <NomRegion(optionnel , default si absent)>
	 * - /land claim setregion <NomRegion>
	 * - /land unclaim here
	 * - /land unclaim all	 
	 */

	private BaseComponent[] getCommandUsage(String command, String desc) {
		ComponentBuilder builder = new ComponentBuilder("- ").color(HexColor.MARRON_CLAIR.getColor());
		builder.append(new ComponentBuilder(command)
				.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
				.event(ChatUtils.getShowTextHoverEvent(ChatColor.GRAY + "Clic pour écrire la commande"))
				.color(HexColor.MARRON.getColor()).create());
		builder.append(new ComponentBuilder(" - ").color(HexColor.MARRON_CLAIR.getColor()).append(desc).color(HexColor.MARRON_CLAIR.getColor()).create());
		return builder.create();
	}


	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			if(args.length == 1) {
				return getStartsWithList(Arrays.asList("menu", "map", "claim", "unclaim", "seeclaims"), args[0]);
			}else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("map")) {
					return getStartsWithList(landManager.getLands(player).stream().map(land -> land.getName()).collect(Collectors.toList()), args[1]);
				}
			}
		}
		return null;
	}

	private List<String> getStartsWithList(List<String> list, String with){
		return list.stream().filter(string -> string.toLowerCase().startsWith(with.toLowerCase())).collect(Collectors.toList());
	}

}
