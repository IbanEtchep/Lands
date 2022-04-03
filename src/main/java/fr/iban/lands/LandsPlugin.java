package fr.iban.lands;

import fr.iban.common.data.redis.RedisAccess;
import fr.iban.lands.commands.LandCMD;
import fr.iban.lands.commands.LandsCMD;
import fr.iban.lands.commands.MaxClaimsCMD;
import fr.iban.lands.listeners.*;
import fr.iban.lands.storage.DbTables;
import fr.iban.lands.storage.Storage;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.LandSyncMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LandsPlugin extends JavaPlugin {

	private LandManager landManager;
	private static LandsPlugin instance;
	private List<UUID> bypass;

	private RedissonClient redisClient;
	private RTopic landSyncTopic;
	private LandSyncListener landSyncListener;


	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		this.bypass = new ArrayList<>();

		if (getConfig().getBoolean("sync-enabled")) {
			try {
				redisClient = RedisAccess.getInstance().getRedissonClient();
				landSyncTopic = redisClient.getTopic("SyncLand");
				landSyncListener = new LandSyncListener(this);
				landSyncTopic.addListener(LandSyncMessage.class, new LandSyncListener(this));
			}catch (Exception e) {
				getLogger().severe("Erreur lors de l'initialisation de la connexion redis.");
			}
		}

		DbTables tables = new DbTables();
		tables.create();
		Storage storage = new Storage();
		landManager = new LandManager(this, storage);
		landManager.loadData();


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
				new FireListener(this)
				);

		if(getServer().getPluginManager().getPlugin("QuickShop") != null) {
			getServer().getPluginManager().registerEvents(new ShopCreateListener(this), this);
		}
		
		Head.loadAPI();

	}

	@Override
	public void onDisable() {
		if(landSyncTopic != null) {
			landSyncTopic.removeListener(landSyncListener);
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

	public RedissonClient getRedisClient() {
		return redisClient;
	}
}
