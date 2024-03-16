package fr.iban.lands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.api.LandService;
import fr.iban.lands.commands.LandCommand;
import fr.iban.lands.commands.LandsCommand;
import fr.iban.lands.commands.MaxClaimsCommand;
import fr.iban.lands.commands.MiscellaneousCommands;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.guild.GuildsDataAccess;
import fr.iban.lands.listeners.*;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.service.LandRepositoryImpl;
import fr.iban.lands.service.LandServiceImpl;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.LandMap;
import fr.iban.lands.utils.SeeChunks;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LandsPlugin extends JavaPlugin {

    private static LandsPlugin instance;
    private List<UUID> bypass;
    private List<UUID> debugPlayers;
    private AbstractGuildDataAccess guildDataAccess;
    private Economy econ = null;
    private ExecutorService singleThreadExecutor;
    private ExecutorService executorService;

    private LandRepository landRepository;

    private LandService landService;

    //TODO rename above
    private final Map<UUID, SeeChunks> seeChunks = new HashMap<>();
    private LandMap landMap;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        this.bypass = new ArrayList<>();
        this.debugPlayers = new ArrayList<>();
        this.singleThreadExecutor = Executors.newSingleThreadExecutor();
        this.executorService = Executors.newCachedThreadPool();

        landRepository = new LandRepositoryImpl(this);
        landService = new LandServiceImpl(this);

        landMap = new LandMap(landRepository);

        setupEconomy();
        hookGuilds();

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
                new TeleportListener(this)
        );

        if (getServer().getPluginManager().getPlugin("QuickShop") != null) {
            getServer().getPluginManager().registerEvents(new ShopListeners(this), this);
            getLogger().info("Intégration QuickShop effectuée.");
        }

        Head.loadAPI();
    }

    @Override
    public void onDisable() {
        singleThreadExecutor.shutdown();
    }

    private void registerCommands() {
        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.accept(CoreBukkitPlugin.getInstance().getCommandHandlerVisitor());

        commandHandler.getAutoCompleter().registerParameterSuggestions(Land.class, (args, sender, command) -> {
            Player player = ((BukkitCommandActor) sender).getAsPlayer();

            if (player == null) return new ArrayList<>();

            return landRepository.getManageableLandsNames(player);
        });

        commandHandler.registerValueResolver(Land.class, context -> {
            String value = context.arguments().pop();
            CommandActor actor = context.actor();
            Player sender = ((BukkitCommandActor) actor).getAsPlayer();
            Land land = landRepository.getManageableLandFromName(sender, value);
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
        if (!getConfig().getBoolean("guild-lands-enabled", false)) {
            return;
        }

        Plugin guildsPlugin = getServer().getPluginManager().getPlugin("Guilds");

        if (guildsPlugin == null || !guildsPlugin.isEnabled()) {
            return;
        }

        GuildsDataAccess guildsDataAccess = new GuildsDataAccess(this);
        guildsDataAccess.load();
        if (guildsDataAccess.isEnabled()) {
            this.guildDataAccess = guildsDataAccess;
        }
    }

    public boolean isGuildsHookEnabled() {
        return guildDataAccess != null && guildDataAccess.isEnabled();
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
    }

    public AbstractGuildDataAccess getGuildDataAccess() {
        return guildDataAccess;
    }

    public String getServerName() {
        return CoreBukkitPlugin.getInstance().getServerName();
    }

    public void runAsyncQueued(Runnable runnable) {
        singleThreadExecutor.execute(runnable);
    }

    public LandRepository getLandRepository() {
        return landRepository;
    }

    public LandService getLandService() {
        return landService;
    }

    public LandMap getLandMap() {
        return landMap;
    }

    public Map<UUID, SeeChunks> getSeeChunks() {
        return seeChunks;
    }
}
