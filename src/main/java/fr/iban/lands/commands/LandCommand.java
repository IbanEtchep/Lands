package fr.iban.lands.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.HexColor;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.PlayerLand;
import fr.iban.lands.objects.SChunk;
import fr.iban.lands.objects.SystemLand;
import fr.iban.lands.utils.ChatUtils;
import fr.iban.lands.utils.LandMap;
import fr.iban.lands.utils.SeeChunks;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Command("land")
public class LandCommand {

    private final LandManager landManager;
    private final LandsPlugin plugin;

    private final Map<UUID, SeeChunks> seeChunks = new HashMap<>();

    public LandCommand(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landManager = plugin.getLandManager();
    }

    @Command("land")
    @Default
    public void land(Player player) {
        help(player);
    }

    @Subcommand("claim")
    public void claim(Player player, @Optional Land withLand) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (withLand == null) {
            landManager.getPlayerFirstLand(player).thenAccept(land -> landManager.claim(player, new SChunk(player.getLocation().getChunk()), land, true));
        } else {
            landManager.claim(player, new SChunk(player.getLocation().getChunk()), withLand, true);
        }
    }

    @Subcommand("unclaim")
    public void unclaim(Player player) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        landManager.unclaim(player, new SChunk(player.getLocation().getChunk()), true);
    }

    @Subcommand("forceunclaim")
    @CommandPermission("lands.admin")
    @SecretCommand
    public void forceunclaim(Player player) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        landManager.unclaim(player.getLocation().getChunk());
        player.sendMessage("§aLe claim a été retiré.");
    }

    @Subcommand("kick")
    public void kick(Player player, Player target) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (!target.getUniqueId().equals(player.getUniqueId())) {
            Land land = landManager.getLandAt(target.getLocation());
            if (land instanceof PlayerLand pland) {
                if (pland.getOwner() != null) {
                    if (pland.getOwner().equals(player.getUniqueId())) {
                        target.teleportAsync(plugin.getConfig().getLocation("spawn-location", Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation()));
                        target.sendMessage("§cVous avez été expulsé du territoire de " + player.getName());
                        player.sendActionBar(Component.text("§aLe joueur a bien été expulsé."));
                    } else {
                        player.sendMessage("§cLe joueur n'est pas dans votre territoire !");
                    }
                }
            }
        } else {
            player.sendMessage("§cImpossible de faire cela sur vous même...");
        }
    }

    @Subcommand("bypass")
    @CommandPermission("lands.bypass")
    public void bypass(Player player, @Optional String landName) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (plugin.getBypass().contains(player.getUniqueId())) {
            plugin.getBypass().remove(player.getUniqueId());
        } else {
            plugin.getBypass().add(player.getUniqueId());
        }
        player.sendMessage("§8§lBypass : " + (plugin.isBypassing(player) ? "§aActivé" : "§cDésactivé"));
    }

    @Subcommand("create")
    public void create(Player player, @Optional String name) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (name == null) {
            player.sendMessage("/land create <NomDeLaRegion>");
        } else {
            landManager.createLand(player, name);
        }
    }

    @Subcommand("claimat")
    @SecretCommand
    public void claimat(Player player, World world, int X, int Z) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        LandMap map = landManager.getLandMap();
        if (!map.getLandMapSelection().isEmpty() && map.getLandMapSelection().containsKey(player.getUniqueId())) {
            Land land = map.getLandMapSelection().get(player.getUniqueId());
            if (land != null) {
                landManager.claim(player, new SChunk(LandsPlugin.getInstance().getServerName(), world.getName(), X, Z), land, true).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    map.display(player, land);
                }));
            }
        }
    }

    @Subcommand("unclaimat")
    @SecretCommand
    public void unclaimat(Player player, World world, int X, int Z) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        LandMap map = landManager.getLandMap();
        if (!map.getLandMapSelection().isEmpty() && map.getLandMapSelection().containsKey(player.getUniqueId())) {
            Land land = map.getLandMapSelection().get(player.getUniqueId());
            if (land != null) {
                if (land instanceof PlayerLand) {
                    landManager.unclaim(player, new SChunk(LandsPlugin.getInstance().getServerName(), world.getName(), X, Z), land, true);
                } else if (land instanceof SystemLand && player.hasPermission("lands.admin")) {
                    landManager.unclaim(world.getChunkAt(X, Z));
                }
                player.sendActionBar("§a§lLe tronçon a bien été unclaim.");
                landManager.unclaim(player, new SChunk(LandsPlugin.getInstance().getServerName(), world.getName(), X, Z), land, true).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    map.display(player, land);
                }));
            }
        }
    }

    @Subcommand("map")
    public void map(Player player, @Optional Land land) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (land == null) {
            landManager.getLandMap().display(player, null);
        } else {
            landManager.getLandMap().display(player, land);
        }
    }

    @Subcommand("seeclaims")
    public void seeclaims(Player player) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!seeChunks.containsKey(uuid)) {
            SeeChunks sc = new SeeChunks(player, landManager);
            seeChunks.put(player.getUniqueId(), sc);
            sc.showParticles();
            player.sendMessage("§aMode vision de claims activé.");
        } else {
            SeeChunks sc = seeChunks.get(uuid);
            sc.stop();
            seeChunks.remove(uuid);
            player.sendMessage("§cMode vision de claims désactivé.");
        }
    }

    @Subcommand("menu")
    public void menu(Player player) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        player.performCommand("lands");
    }

    @Subcommand("pay")
    public void pay(Player player, Land land) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        if (!land.isPaymentDue()) {
            player.sendMessage("§cCe territoire n'a pas de paiement en attente.");
            return;
        }

        if (landManager.handlePayment(land)) {
            player.sendMessage("§aLa transaction s'est déroulée avec succès. Le territoire est débloqué.");
        } else {
            player.sendMessage("§cLe paiement n'a pas pu être effectué. Vérifiez que les fonds nécessaires sont disponibles.");
        }
    }

    @Subcommand("admin setspawn")
    @CommandPermission("lands.admin")
    public void setSpawnLocation(Player player) {
        plugin.getConfig().set("spawn-location", player.getLocation());
        plugin.saveConfig();
        player.sendMessage("§aPosition du spawn redéfini.");
    }

    @Subcommand("help")
    public void help(Player player) {
        if (!plugin.getConfig().getBoolean("players-lands-enabled") && !player.hasPermission("lands.bypass")) {
            player.sendMessage("§cLes territoires ne sont pas activés sur ce serveur.");
            return;
        }

        player.sendMessage(HexColor.MARRON_CLAIR.getColor() + "La protection de vos territoires se gère avec les commandes ci-dessous.");
        player.sendMessage("");
        player.sendMessage(getCommandUsage("/lands", "Ouvre le menu de gestion de vos territoires."));
        player.sendMessage(getCommandUsage("/land claim <territoire(optionnel)>", "Attribue le tronçon où vous vous trouvez au territoire choisi ou à votre premier territoire."));
        player.sendMessage(getCommandUsage("/land unclaim", "Retire le tronçon où vous vous trouvez de vos territoires."));
        player.sendMessage(getCommandUsage("/land map <territoire(optionnel)>", "Affiche une carte des territoires alentours. Permet de claim/unclaim si un territoire est selectionné."));
        player.sendMessage(getCommandUsage("/land kick <joueur> ", "Renvois un joueur qui se trouve dans votre territoire au spawn."));
        player.sendMessage(getCommandUsage("/land seeclaims ", "Permet de voir les bordures des claims."));
        player.sendMessage("");
        player.sendMessage(HexColor.MARRON_CLAIR.getColor() + "Les " + HexColor.MARRON.getColor() + "tronçons(chunks) " + HexColor.MARRON_CLAIR.getColor() + "mesurent "
                + HexColor.MARRON.getColor() + "16x256x16 blocs" + HexColor.MARRON_CLAIR.getColor() + " et sont visible en appuyant sur les touche "
                + HexColor.MARRON.getColor() + "F3+G" + HexColor.MARRON_CLAIR.getColor() + ".");
    }

    private BaseComponent[] getCommandUsage(String command, String desc) {
        ComponentBuilder builder = new ComponentBuilder("- ").color(HexColor.MARRON_CLAIR.getColor());
        builder.append(new ComponentBuilder(command)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .event(ChatUtils.getShowTextHoverEvent(ChatColor.GRAY + "Clic pour écrire la commande"))
                .color(HexColor.MARRON.getColor()).create());
        builder.append(new ComponentBuilder(" - ").color(HexColor.MARRON_CLAIR.getColor()).append(desc).color(HexColor.MARRON_CLAIR.getColor()).create());
        return builder.create();
    }

}
