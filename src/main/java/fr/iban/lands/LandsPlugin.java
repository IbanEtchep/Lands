package fr.iban.lands;

import com.alessiodp.parties.api.Parties;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.guilds.GuildsPlugin;
import fr.iban.lands.commands.LandCommand;
import fr.iban.lands.commands.LandsCommand;
import fr.iban.lands.commands.MaxClaimsCommand;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.guild.GuildsDataAccess;
import fr.iban.lands.guild.PartiesDataAccess;
import fr.iban.lands.listeners.*;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.Head;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class LandsPlugin extends JavaPlugin {

    private LandManager landManager;
    private static LandsPlugin instance;
    private List<UUID> bypass;
    private AbstractGuildDataAccess guildDataAccess;
    public static final String SYNC_CHANNEL = "LandSync";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.bypass = new ArrayList<>();

        hookGuilds();

        landManager = new LandManager(this);

        if (getConfig().getBoolean("sync-enabled")) {
            getServer().getPluginManager().registerEvents(new LandSyncListener(landManager), this);
        }

        registerCommands();

        registerListeners(
                new PlayerMoveListener(this),
                new PlayerTakeLecternBookListener(this),
                new BlockPlaceListener(this),
                new BlockBreakListener(this),
                new PistonListeners(this),
                new InteractListener(this),
                new EntitySpawnListener(this),
                new EntityExplodeListener(this),
                new DamageListeners(this),
                new EntityBlockDamageListener(this),
                new CommandListener(this),
                new HangingListeners(this),
                new TeleportListener(this),
                new DropListener(this),
                new LandListeners(this),
                new HeadDatabaseListener(),
                new PortalListeners(this),
                new FireListener(this)
        );

        if (getServer().getPluginManager().getPlugin("QuickShop") != null) {
            getServer().getPluginManager().registerEvents(new ShopCreateListener(this), this);
        }

        Head.loadAPI();

    }

    private void registerCommands() {
        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.accept(CoreBukkitPlugin.getInstance().getCommandHandlerVisitor());

        //Land resolver
        commandHandler.getAutoCompleter().registerParameterSuggestions(Land.class, (args, sender, command) -> {
            Player player = ((BukkitCommandActor)sender).getAsPlayer();
            return landManager.getManageableLandsNames(player);
        });

        commandHandler.registerValueResolver(Land.class, context -> {
            String value = context.arguments().pop();
            CommandActor actor = context.actor();
            Player sender = ((BukkitCommandActor)actor).getAsPlayer();
            Land land = getLandManager().getManageableLandFromName(sender, value);
            if (land == null) {
                throw new CommandErrorException("Le territoire " + value + " n'existe pas.");
            }
            return land;
        });

        commandHandler.register(new LandCommand(this));
        commandHandler.register(new LandsCommand(this));
        commandHandler.register(new MaxClaimsCommand());
        //commandHandler.registerBrigadier();
    }

    private void hookGuilds() {
        if (getConfig().getBoolean("guild-lands-enabled", false)) {
            if (getServer().getPluginManager().getPlugin("Parties") != null) {
                if (Objects.requireNonNull(getServer().getPluginManager().getPlugin("Parties")).isEnabled()) {
                    guildDataAccess = new PartiesDataAccess(Parties.getApi(), this);
                    getLogger().info("Intégration avec le plugin Parties effectué.");
                    getServer().getPluginManager().registerEvents((PartiesDataAccess) guildDataAccess, this);
                }
            }
            if (getServer().getPluginManager().getPlugin("Guilds") != null) {
                if (Objects.requireNonNull(getServer().getPluginManager().getPlugin("Guilds")).isEnabled()) {
                    guildDataAccess = new GuildsDataAccess(GuildsPlugin.getInstance(), this);
                    getLogger().info("Intégration avec le plugin Guilds effectué.");
                    getServer().getPluginManager().registerEvents((GuildsDataAccess) guildDataAccess, this);
                }
            }
        }
    }

    public LandManager getLandManager() {
        return landManager;
    }

    public static LandsPlugin getInstance() {
        return instance;
    }

    private void registerListeners(Listener... listeners) {

        PluginManager pm = Bukkit.getPluginManager();

        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }

    }

    public List<UUID> getBypass() {
        return bypass;
    }

    public boolean isBypassing(Player player) {
        return getBypass().contains(player.getUniqueId());
    }

    public AbstractGuildDataAccess getGuildDataAccess() {
        return guildDataAccess;
    }

}
