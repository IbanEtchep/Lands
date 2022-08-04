package fr.iban.lands.commands;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.menus.LandMainMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.UUID;

@Command("lands")
public class LandsCommand {

    private final LandsPlugin plugin;
    private final LandManager landManager;

    public LandsCommand(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landManager = plugin.getLandManager();
    }

    @Command("lands")
    @Default
    public void lands(Player player, @Optional OfflinePlayer target) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.admin")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (target == null) {
            landManager.getLandsAsync(player).thenAccept(lands -> {
                Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.PLAYER).open());
            });
        }

        if (target != null && player.hasPermission("lands.admin")) {
            landManager.getLandsAsync(target.getUniqueId()).thenAccept(lands -> {
                Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.PLAYER).open());
            });
        }
    }

    @Subcommand("system")
    @CommandPermission("lands.admin")
    public void landsSystem(Player player) {
        landManager.getSystemLandsAsync().thenAccept(lands -> {
            Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.SYSTEM).open());
        });
    }

    @Subcommand("guild")
    public void landsGuild(Player player) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") || plugin.getGuildDataAccess() == null) {
            player.sendMessage("§cLes territoires de guilde ne sont pas activés sur ce serveur.");
            return;
        }

        AbstractGuildDataAccess guildDataAccess = plugin.getGuildDataAccess();
        UUID guildId = guildDataAccess.getGuildId(player.getUniqueId());
        if (guildId != null && guildDataAccess.canManageGuildLand(player.getUniqueId())) {
            landManager.getGuildLandsAsync(guildId).thenAccept(lands -> {
                Bukkit.getScheduler().runTask(plugin, () -> new LandMainMenu(player, plugin, lands, LandType.GUILD).open());
            });
        } else {
            player.sendMessage("§cVous devez être administrateur d'une guilde pour accéder à ce menu.");
        }
    }


}
