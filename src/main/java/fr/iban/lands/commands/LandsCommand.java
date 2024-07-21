package fr.iban.lands.commands;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.menus.LandMainMenu;
import fr.iban.lands.model.land.Land;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Command("lands")
public class LandsCommand {

    private final LandsPlugin plugin;
    private final LandRepository landRepository;
    private final boolean playerLandsEnabled;

    public LandsCommand(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landRepository = plugin.getLandRepository();
        this.playerLandsEnabled = plugin.getConfig().getBoolean("players-lands-enabled");
    }

    @Subcommand("menu")
    @DefaultFor("lands")
    public void lands(Player player, @Optional OfflinePlayer target) {
        if (!playerLandsEnabled && !player.hasPermission("lands.admin")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (target != null && !player.hasPermission("lands.admin")) {
            return;
        }

        UUID targetId = target == null ? player.getUniqueId() : target.getUniqueId();

        List<Land> lands = new ArrayList<>(landRepository.getLands(targetId, LandType.PLAYER));
        LandMainMenu landMainMenu = new LandMainMenu(player, plugin, lands, LandType.PLAYER, targetId);
        landMainMenu.open();
    }

    @Subcommand("system")
    @CommandPermission("lands.admin")
    public void landsSystem(Player player) {
        List<Land> systemLands = new ArrayList<>(landRepository.getSystemLands());
        new LandMainMenu(player, plugin, systemLands, LandType.SYSTEM, null).open();
    }

    @Subcommand("guild")
    public void landsGuild(Player player) {
        if (!playerLandsEnabled || !plugin.isGuildsHookEnabled()) {
            player.sendMessage("§cLes territoires de guilde ne sont pas activés sur ce serveur.");
            return;
        }

        AbstractGuildDataAccess guildDataAccess = plugin.getGuildDataAccess();
        UUID guildId = guildDataAccess.getGuildId(player.getUniqueId());

        if (guildId != null && guildDataAccess.canManageGuildLand(player.getUniqueId())) {
            List<Land> guildLands = new ArrayList<>(landRepository.getGuildLands(guildId));
            LandMainMenu landMainMenu = new LandMainMenu(player, plugin, guildLands, LandType.GUILD, guildId);
            landMainMenu.open();
        } else {
            player.sendMessage("§cVous devez être administrateur d'une guilde pour accéder à ce menu.");
        }
    }
}
