package fr.iban.lands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.commands.LandCommand;
import fr.iban.lands.commands.LandsCommand;
import fr.iban.lands.commands.MaxClaimsCommand;
import fr.iban.lands.commands.MiscellaneousCommands;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.guild.GuildsDataAccess;
import fr.iban.lands.land.Land;
import fr.iban.lands.listeners.*;
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

    private static LandsPlugin instance;
    private LandManager landManager;
    private List<UUID> bypass;
    private List<UUID> debugPlayers;
    private AbstractGuildDataAccess guildDataAccess;
    public static final String LAND_SYNC_CHANNEL = "LandSync";
    public static final String CHUNK_SYNC_CHANNEL = "LandChunkSync";
    public static final String BYPASS_SYNC_CHANNEL = "ToggleLandBypassSync";
    public static final String DEBUG_SYNC_CHANNEL = "ToggleLandDebugSync";
    private Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.bypass = new ArrayList<>();
        this.debugPlayers = new ArrayList<>();

        landManager = new LandManager(this);

        setupEconomy();
        hookGuilds();

        if (getConfig().getBoolean("sync-enabled")) {
            getServer().getPluginManager().registerEvents(new LandSyncListener(this), this);
        }

        registerCommands();

        registerListeners(
                new BlockBreakListener(this),
                new BlockPlaceListener(this),
                new CommandListener(this),
                new DamageListeners(this),
                new DropListener(this),
                new EntityBlockDamageListener(this),
                new EntitySpawnListener(this),
                new ExplodeListeners(this),
                new FireListener(this),
                new HangingListeners(this),
                new HeadDatabaseListener(),
                new InteractListener(this),
                new JoinQuitListeners(this),
                new LandListeners(this),
                new PistonListeners(this),
                new PlayerMoveListener(this),
                new PlayerTakeLecternBookListener(this),
                new PortalListeners(this),
                new ServiceListeners(this),
                new SignListeners(this),
                new TeleportListener(this));

        if (getServer().getPluginManager().getPlugin("QuickShop") != null) {
            getServer().getPluginManager().registerEvents(new ShopListeners(this), this);
            getLogger().info("Intégration QuickShop effectuée.");
        }

        Head.loadAPI();
    }

    private void registerCommands() {
        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.accept(CoreBukkitPlugin.getInstance().getCommandHandlerVisitor());

        // Land resolver
        commandHandler
                .getAutoCompleter()
                .registerParameterSuggestions(
                        Land.class,
                        (args, sender, command) -> {
                            Player player = ((BukkitCommandActor) sender).getAsPlayer();
                            return landManager.getManageableLandsNames(player);
                        });

        commandHandler.registerValueResolver(
                Land.class,
                context -> {
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
        commandHandler.register(new MaxClaimsCommand(this));
        commandHandler.register(new MiscellaneousCommands());
        commandHandler.registerBrigadier();
    }

    public void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
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
                if (Objects.requireNonNull(getServer().getPluginManager().getPlugin("Guilds"))
                        .isEnabled()) {
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
        return isBypassing(player.getUniqueId());
    }

    public boolean isBypassing(UUID uuid) {
        return getBypass().contains(uuid);
    }

    public void setBypassing(UUID uuid, boolean value) {
        if (value) {
            bypass.add(uuid);
        } else {
            bypass.remove(uuid);
        }
        if (isMultipaperSupportEnabled()) {
            CoreBukkitPlugin.getInstance()
                    .getMessagingManager()
                    .sendMessage(LandsPlugin.BYPASS_SYNC_CHANNEL, uuid.toString());
        }
    }

    public List<UUID> getDebugPlayers() {
        return debugPlayers;
    }

    public boolean isInDebugMode(Player player) {
        return isInDebugMode(player.getUniqueId());
    }

    public boolean isInDebugMode(UUID uuid) {
        return getDebugPlayers().contains(uuid);
    }

    public void setDebugging(UUID uuid, boolean value) {
        if (value) {
            debugPlayers.add(uuid);
        } else {
            debugPlayers.remove(uuid);
        }
        if (isMultipaperSupportEnabled()) {
            CoreBukkitPlugin.getInstance()
                    .getMessagingManager()
                    .sendMessage(LandsPlugin.DEBUG_SYNC_CHANNEL, uuid.toString());
        }
    }

    public AbstractGuildDataAccess getGuildDataAccess() {
        return guildDataAccess;
    }

    public boolean isMultipaperSupportEnabled() {
        return getConfig().getBoolean("multipaper-support.enabled", false);
    }

    public @Nullable String getMultipaperServerName() {
        return isMultipaperSupportEnabled()
                ? getConfig().getString("multipaper-support.server-name")
                : null;
    }

    public String getServerName() {
        String multipaperServer = getMultipaperServerName();
        return multipaperServer != null ? multipaperServer : CoreBukkitPlugin.getInstance().getServerName();
    }
}
