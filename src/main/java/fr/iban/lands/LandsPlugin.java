package fr.iban.lands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.commands.LandCommand;
import fr.iban.lands.commands.LandsCommand;
import fr.iban.lands.commands.MaxClaimsCommand;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.guild.GuildsDataAccess;
import fr.iban.lands.listeners.*;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.Head;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
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
    public static final String LAND_SYNC_CHANNEL = "LandSync";
    public static final String CHUNK_SYNC_CHANNEL = "LandChunkSync";
    private Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.bypass = new ArrayList<>();

        landManager = new LandManager(this);

        setupEconomy();
        hookGuilds();

        if (getConfig().getBoolean("sync-enabled")) {
            getServer().getPluginManager().registerEvents(new LandSyncListener(this), this);
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
                new FireListener(this),
                new ServiceListeners(this)
        );

        if (getServer().getPluginManager().getPlugin("QuickShop") != null) {
            getServer().getPluginManager().registerEvents(new ShopListeners(this), this);
            getLogger().info("Intégration QuickShop effectuée.");
        }

        Head.loadAPI();

    }

    private void registerCommands() {
        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.accept(CoreBukkitPlugin.getInstance().getCommandHandlerVisitor());

        //Land resolver
        commandHandler.getAutoCompleter().registerParameterSuggestions(Land.class, (args, sender, command) -> {
            Player player = ((BukkitCommandActor) sender).getAsPlayer();
            return landManager.getManageableLandsNames(player);
        });

        commandHandler.registerValueResolver(Land.class, context -> {
            String value = context.arguments().pop();
            CommandActor actor = context.actor();
            Player sender = ((BukkitCommandActor) actor).getAsPlayer();
            Land land = getLandManager().getManageableLandFromName(sender, value);
            if (land == null) {
                throw new CommandErrorException("Le territoire " + value + " n''existe pas.");
            }
            return land;
        });

        commandHandler.register(new LandCommand(this));
        commandHandler.register(new LandsCommand(this));
        commandHandler.register(new MaxClaimsCommand());
        commandHandler.registerBrigadier();
    }

    public void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public Economy getEconomy() {
        return econ;
    }

    private void hookGuilds() {
        if (getConfig().getBoolean("guild-lands-enabled", false)) {
            if (getServer().getPluginManager().getPlugin("Guilds") != null) {
                if (Objects.requireNonNull(getServer().getPluginManager().getPlugin("Guilds")).isEnabled()) {
                    GuildsDataAccess guildsDataAccess = new GuildsDataAccess(this);
                    guildsDataAccess.load();
                    if (guildsDataAccess.isEnabled()) {
                        this.guildDataAccess = guildsDataAccess;
                    }
                }
            }
        }
    }

    public boolean isGuildsHookEnabled() {
        return guildDataAccess != null && guildDataAccess.isEnabled();
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

    public boolean isMultipaperSupportEnabled() {
        return getConfig().getBoolean("multipaper-support.enabled", false);
    }

    public @Nullable String getMultipaperServerName() {
        return isMultipaperSupportEnabled() ? getConfig().getString("multipaper-support.server-name") : null;
    }

    public String getServerName() {
        String multipaperServer = getMultipaperServerName();
        return multipaperServer != null ? multipaperServer : CoreBukkitPlugin.getInstance().getServerName();
    }

}
