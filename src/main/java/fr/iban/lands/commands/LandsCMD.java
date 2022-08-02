package fr.iban.lands.commands;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.menus.LandMainMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class LandsCMD implements CommandExecutor, TabCompleter {

    private LandManager landManager;
    private LandsPlugin plugin;

    public LandsCMD(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landManager = plugin.getLandManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (args.length == 0 && plugin.getConfig().getBoolean("players-lands-enabled")) {
                landManager.getLandsAsync(player).thenAccept(lands -> {
                    Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.PLAYER).open());
                });
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("guild") && plugin.getConfig().getBoolean("players-lands-enabled") && plugin.getGuildDataAccess() != null) {
                    AbstractGuildDataAccess guildDataAccess = plugin.getGuildDataAccess();
                    UUID guildId = guildDataAccess.getGuildId(player.getUniqueId());
                    if (guildId != null && guildDataAccess.canManageGuildLand(player.getUniqueId(), guildId)) {
                        landManager.getGuildLandsAsync(guildId).thenAccept(lands -> {
                            Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.GUILD).open());
                        });
                    } else {
                        player.sendMessage("§cVous devez être modérateur d'une guilde pour accéder à ce menu.");
                    }
					return false;
                }
                if (player.hasPermission("lands.admin")) {
                    if (args[0].equalsIgnoreCase("system")) {
                        landManager.getSystemLandsAsync().thenAccept(lands -> {
                            Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.SYSTEM).open());
                        });
                    } else {
                        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
                        if (target != null) {
                            landManager.getLandsAsync(target.getUniqueId()).thenAccept(lands -> {
                                Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.PLAYER).open());
                            });
                        } else {
                            player.sendMessage("§cCe joueur n'a jamais joué !");
                        }
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
