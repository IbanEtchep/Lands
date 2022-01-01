package fr.iban.lands.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.menus.LandMainMenu;

public class LandsCMD implements CommandExecutor, TabCompleter {

	private LandManager landManager;
	private LandsPlugin plugin;

	public LandsCMD(LandsPlugin plugin) {
		this.plugin = plugin;
		this.landManager = plugin.getLandManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {

			Player player = (Player)sender;

			if(args.length == 0 && plugin.getConfig().getBoolean("players-lands-enabled")) {
				landManager.getLandsAsync(player).thenAccept(lands -> {
					Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.PLAYER).open());	
				});		
			}else if(args.length == 1 && player.hasPermission("lands.admin")) {
				if(args[0].equalsIgnoreCase("system")) {
					landManager.getSystemLandsAsync().thenAccept(lands -> {
						Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.SYSTEM).open());	
					});	
				}else {
					@SuppressWarnings("deprecation")
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
					if(target.hasPlayedBefore()) {
						landManager.getLandsAsync(target.getUniqueId()).thenAccept(lands -> {
							Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.PLAYER).open());	
						});		
					}else {
						player.sendMessage("§cCe joueur n'est pas en ligne !");
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
