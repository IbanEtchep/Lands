package fr.iban.lands;

import com.alessiodp.parties.api.Parties;
import fr.iban.common.data.redis.RedisAccess;
import fr.iban.lands.commands.LandCMD;
import fr.iban.lands.commands.LandsCMD;
import fr.iban.lands.commands.MaxClaimsCMD;
import fr.iban.lands.guild.GuildDataAccess;
import fr.iban.lands.guild.PartiesDataAccess;
import fr.iban.lands.listeners.*;
import fr.iban.lands.utils.Head;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class LandsPlugin extends JavaPlugin {

    private LandManager landManager;
    private static LandsPlugin instance;
    private List<UUID> bypass;
    private GuildDataAccess guildDataAccess;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.bypass = new ArrayList<>();

        if (getConfig().getBoolean("guild-lands-enabled", false)) {
            hookGuilds();
        }

        if (getConfig().getBoolean("sync-enabled")) {
            getServer().getPluginManager().registerEvents(new LandSyncListener(landManager), this);
        }
        landManager = new LandManager(this);

        getCommand("land").setExecutor(new LandCMD(this));
        getCommand("land").setTabCompleter(new LandCMD(this));

        getCommand("lands").setExecutor(new LandsCMD(this));
        getCommand("lands").setTabCompleter(new LandsCMD(this));

        getCommand("addmaxclaim").setExecutor(new MaxClaimsCMD());
        getCommand("removemaxclaim").setExecutor(new MaxClaimsCMD());
        getCommand("getmaxclaim").setExecutor(new MaxClaimsCMD());

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
                new GuildEvents(this)
        );

        if (getServer().getPluginManager().getPlugin("QuickShop") != null) {
            getServer().getPluginManager().registerEvents(new ShopCreateListener(this), this);
        }

        Head.loadAPI();

    }

    private void hookGuilds() {
        if (getServer().getPluginManager().getPlugin("Parties") != null) {
            if (Objects.requireNonNull(getServer().getPluginManager().getPlugin("Parties")).isEnabled()) {
                guildDataAccess = new PartiesDataAccess(Parties.getApi());
                getLogger().info("Intégration avec le plugin Parties effectué.");
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

    public GuildDataAccess getGuildDataAccess() {
        return guildDataAccess;
    }

}
