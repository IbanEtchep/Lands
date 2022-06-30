package fr.iban.lands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.AccountManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import fr.iban.lands.objects.*;
import fr.iban.lands.storage.AbstractStorage;
import fr.iban.lands.storage.DbTables;
import fr.iban.lands.storage.Storage;
import fr.iban.lands.utils.Cuboid;
import fr.iban.lands.utils.LandMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LandManager {

    private final AbstractStorage storage;
    private boolean loaded = false;

    private final Map<Integer, Land> lands = new ConcurrentHashMap<>();
    private final Map<SChunk, Land> chunks = new ConcurrentHashMap<>();
    private final LandMap landMap;
    private final LandsPlugin plugin;
    private SystemLand wilderness = new SystemLand(-1, "Zone sauvage");

    public LandManager(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landMap = new LandMap(this);
        DbTables tables = new DbTables();
        tables.create();
        storage = new Storage();
        loadData();
    }

    /*
     * Charge les données depuis la bdd
     */
    public void loadData() {
        final long start = System.currentTimeMillis();
        plugin.getLogger().log(Level.INFO, "Chargement des données :.");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            plugin.getLogger().log(Level.INFO, "Chargement des lands...");
            getLands().putAll(storage.getLands());

            plugin.getLogger().log(Level.INFO, "Chargement des chunks...");
            Map<SChunk, Integer> savedchunks = storage.getChunks();
            for (Entry<SChunk, Integer> entry : savedchunks.entrySet()) {
                if (lands.containsKey(entry.getValue())) {
                    getChunks().put(entry.getKey(), getLands().get(entry.getValue()));
                }
            }
            plugin.getLogger().log(Level.INFO, getChunks().size() + " chunks chargées");

            plugin.getLogger().log(Level.INFO, "Chargement des liens...");
            storage.loadLinks(this);
            getSystemLand("Zone sauvage").thenAccept(wild -> {
                if (wild == null) {
                    saveWilderness(wilderness);
                } else {
                    wilderness = wild;
                }
            });
            loaded = true;
            plugin.getLogger().info("Chargement des données terminé en " + (System.currentTimeMillis() - start) + " ms.");
        });
    }

    /*
     * Retourne la liste de tous les territoires.
     */
    public Map<Integer, Land> getLands() {
        return lands;
    }

    /*
     * Retourne la liste de tous les territoires d'un joueur.
     */
    public List<PlayerLand> getLands(UUID uuid) {
        return getLands().values().stream()
                .filter(PlayerLand.class::isInstance)
                .map(PlayerLand.class::cast)
                .filter(l -> l.getOwner().equals(uuid))
                .collect(Collectors.toList());
    }


    /*
     * Retourne la liste de toutes les territoires d'un joueur.
     */
    public List<PlayerLand> getLands(Player player) {
        UUID uuid = player.getUniqueId();
        return getLands(uuid);
    }

    /*
     * Retourne la liste de toutes les territoires d'un joueur. (ASYNC)
     */
    public CompletableFuture<List<Land>> getLandsAsync(Player player) {
        return future(() -> new ArrayList<>(getLands(player)));
    }

    public CompletableFuture<List<Land>> getLandsAsync(UUID uuid) {
        return future(() -> new ArrayList<>(getLands(uuid)));
    }

    /*
     * Retourne la liste de tous les territoires systeme.
     */
    public List<SystemLand> getSystemLands() {
        return getLands().values().stream()
                .filter(SystemLand.class::isInstance)
                .map(SystemLand.class::cast)
                .collect(Collectors.toList());
    }

    public CompletableFuture<List<Land>> getSystemLandsAsync() {
        return future(() -> new ArrayList<>(getSystemLands()));

    }

    /*
     * Retourne les territoires d'une guilde
     */
    public List<GuildLand> getGuildLands(UUID guildId) {
        return getLands().values().stream()
                .filter(GuildLand.class::isInstance)
                .map(GuildLand.class::cast)
                .filter(guildLand -> guildLand.getGuildId().equals(guildId))
                .collect(Collectors.toList());
    }

    public CompletableFuture<List<Land>> getGuildLandsAsync(UUID guildId) {
        return future(() -> new ArrayList<>(getGuildLands(guildId)));
    }

    //Retourne le territoire du nom donné pour le joueur donné.
    public CompletableFuture<PlayerLand> getPlayerLand(Player player, String name) {
        return future(() -> {
            for (PlayerLand land : getLands(player)) {
                if (land.getName().equalsIgnoreCase(name)) {
                    return land;
                }
            }
            return null;
        });
    }

    //Retourne le territoire du nom donné pour le joueur donné.
    public CompletableFuture<SystemLand> getSystemLand(String name) {
        return future(() -> {
            for (SystemLand land : getSystemLands()) {
                if (land.getName().equalsIgnoreCase(name)) {
                    return land;
                }
            }
            return null;
        });
    }

    //Retourne le territoire du nom donné pour le la guilde donnée.
    public CompletableFuture<GuildLand> getGuildLand(UUID guildID, String name) {
        return future(() -> {
            for (GuildLand land : getGuildLands(guildID)) {
                if (land.getName().equalsIgnoreCase(name)) {
                    return land;
                }
            }
            return null;
        });
    }

    //Retourne le territoire du nom donné pour le joueur donné.
    public CompletableFuture<PlayerLand> getPlayerFirstLand(Player player) {
        return future(() -> {
            List<PlayerLand> plands = getLands(player);
            if (!plands.isEmpty()) {
                return plands.get(0);
            }
            player.sendMessage("§cVous n'avez pas de territoire.");
            return null;
        });
    }

    /*
     * Permet de créer un nouveau territoire en tant que joueur.
     */
    public CompletableFuture<PlayerLand> createLand(Player player, String name) {
        return future(() -> {
            if (getLands(player).size() > 50) {
                player.sendMessage("§cVous pouvez avoir 50 territoires maximum.");
                return null;
            }
            if (getPlayerLand(player, name).get() != null) {
                player.sendMessage("§cVous avez déjà un territoire à ce nom.");
                return null;
            }
            if (name.length() > 16) {
                player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
                return null;
            }
            PlayerLand land = new PlayerLand(-1, player.getUniqueId(), name);
            storage.addLand(land);
            int id = storage.getLandID(land.getType(), land.getOwner(), land.getName());
            land.setId(id);
            getLands().put(id, land);
            land.setBans(new HashSet<>());
            land.setFlags(new HashSet<>());
            player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
            syncLand(land);
            return land;
        });
    }

    /*
     * Permet de créer un nouveau territoire en tant que joueur.
     */
    public CompletableFuture<GuildLand> createGuildLand(Player player, String name) {
        return future(() -> {
            UUID guildId = plugin.getGuildDataAccess().getGuildId(player.getUniqueId());
            if (getGuildLands(guildId).size() > 50) {
                player.sendMessage("§cVous pouvez avoir 50 territoires maximum.");
                return null;
            }
            if (getGuildLand(guildId, name).get() != null) {
                player.sendMessage("§cIl y a déjà un territoire à ce nom.");
                return null;
            }
            if (name.length() > 16) {
                player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
                return null;
            }

            GuildLand land = new GuildLand(-1, guildId, name);
            storage.addLand(land);
            int id = storage.getLandID(land.getType(), land.getOwner(), land.getName());
            land.setId(id);
            getLands().put(id, land);
            land.setBans(new HashSet<>());
            land.setFlags(new HashSet<>());
            player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
            syncLand(land);
            return land;
        });
    }

    /*
     * Permet de créer un nouveau territoire système.
     */
    public CompletableFuture<SystemLand> createSystemLand(Player player, String name) {
        return future(() -> {

            if (getSystemLand(name).get() != null) {
                player.sendMessage("§cIl y a déjà un territoire à ce nom.");
                return null;
            }

            if (name.length() > 16) {
                player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
                return null;
            }
            SystemLand land = new SystemLand(-1, name);
            storage.addLand(land);
            int id = storage.getSystemLandID(name);
            land.setId(id);
            getLands().put(id, land);
            land.setBans(new HashSet<>());
            land.setFlags(new HashSet<>());
            player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
            syncLand(land);
            return land;
        });
    }

    public CompletableFuture<Void> saveWilderness(SystemLand land) {
        return future(() -> {
            getLands().put(-1, land);
            storage.addLand(land);
        });
    }

    /*
     * Permet de créer un nouveau territoire.
     */
    public CompletableFuture<PlayerLand> createLand(UUID uuid, String name) {
        return future(() -> {
            PlayerLand land = new PlayerLand(-1, uuid, name);
            storage.addLand(land);
            int id = storage.getLandID(land.getType(), land.getOwner(), land.getName());
            land.setId(id);
            getLands().put(id, land);
            land.setBans(new HashSet<>());
            land.setFlags(new HashSet<>());
            Bukkit.broadcast(Component.text("§aLe territoire au nom de " + name + " pour " + Bukkit.getOfflinePlayer(uuid).getName() + " a été créée."));
            syncLand(land);
            return land;
        });
    }

    /*
     * Permet de supprimer un territoire
     */
    public CompletableFuture<Void> deleteLand(Land land) {
        return future(() -> {
            if (land == null) {
                return;
            }
            storage.deleteLand(land);
            getChunks(land).forEach(schunk -> {
                getChunks().remove(schunk);
                chunksCache.invalidate(schunk);
            });

            if (land.hasSubLand()) {
                for (SubLand subland : land.getSubLands().values()) {
                    deleteLand(subland);
                }
            }
            syncLand(land);
            getLands().remove(land.getId());
        });
    }


    /*
     * Permet de renommer un territoire
     */
    public CompletableFuture<Void> renameLand(Land land, Player player, String name) {
        return future(() -> {
            try {
                if (getPlayerLand(player, name).get() != null) {
                    player.sendMessage("§cVous avez déjà un territoire à ce nom.");
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (name.length() > 16) {
                player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
                return;
            }
            land.setName(name);
            storage.renameLand(land, name);
            syncLand(land);
            player.sendMessage("§aLe nom du territoire a bien été modifié.");
        });
    }

    /*
     * Retourne le nombre de chunks détenus par un joueur.
     */
    public CompletableFuture<Integer> getChunkCount(Player player) {
        return future(() -> storage.getChunkCount(player.getUniqueId()));
    }

    public int getMaxChunkCount(Player player) {
        if (plugin.isBypassing(player)) {
            return 1000000;
        }
        AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
        return accountManager.getAccount(player.getUniqueId()).getMaxClaims();
    }

    public CompletableFuture<Integer> getRemainingChunkCount(Player player) {
        return future(() -> getMaxChunkCount(player) - getChunkCount(player).get());
    }

    /*
     * Retourne la liste des chunks d'un territoire.
     */
    public Collection<SChunk> getChunks(Land land) {
        Set<SChunk> chunksSet = new HashSet<>();
        for (Entry<SChunk, Land> entry : getChunks().entrySet()) {
            if (land.equals(entry.getValue())) {
                chunksSet.add(entry.getKey());
            }
        }
        return chunksSet;
    }

    /*
     * Retourne tous les chunks claim du serveur.
     */
    public Map<SChunk, Land> getChunks() {
        return chunks;
    }

    /*
     * TRUST / UNTRUST
     */

    public void addTrust(Land land, UUID uuid, Action action) {
        land.trust(uuid, action);
        future(() -> storage.addTrust(land, uuid, action)).thenRun(() -> syncLand(land));
    }

    public void removeTrust(Land land, UUID uuid, Action action) {
        land.untrust(uuid, action);
        future(() -> storage.removeTrust(land, uuid, action)).thenRun(() -> syncLand(land));
    }

    public void addGlobalTrust(Land land, Action action) {
        land.trust(action);
        future(() -> storage.addGlobalTrust(land, action)).thenRun(() -> syncLand(land));
    }

    public void removeGlobalTrust(Land land, Action action) {
        land.untrust(action);
        future(() -> storage.removeGlobalTrust(land, action)).thenRun(() -> syncLand(land));
    }

    public void addGuildTrust(Land land, Action action) {
        land.trustGuild(action);
        future(() -> storage.addGuildTrust(land, action)).thenRun(() -> syncLand(land));
    }

    public void removeGuildTrust(Land land, Action action) {
        land.untrustGuild(action);
        future(() -> storage.removeGuildTrust(land, action)).thenRun(() -> syncLand(land));
    }


    /*
     * CLAIM / UNCLAIM
     */

    private Cache<SChunk, Land> chunksCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public Land getLandAt(Chunk chunk) {
        return getLandAt(new SChunk(chunk));
    }

    public Land getLandAt(SChunk schunk) {
        if (!loaded) {
            return wilderness;
        }
        return chunksCache.get(schunk, land -> getChunks().getOrDefault(schunk, wilderness));
    }

    public Land getLandAt(Location loc) {
        Land land = getLandAt(loc.getChunk());
        SubLand subLand = land.getSubLandAt(loc);
        if (subLand != null) {
            return subLand;
        } else {
            return land;
        }
    }


    public CompletableFuture<Land> getLandAtAsync(Chunk chunk) {
        return future(() -> getLandAt(chunk));
    }

    public CompletableFuture<Land> getLandAtAsync(Location loc) {
        return future(() -> getLandAt(loc));
    }

    /*
     * Ajouter un chunk à un territoire :
     */
    public void claim(SChunk chunk, Land land) {
        getChunks().put(chunk, land);
        chunksCache.invalidate(chunk);
        future(() -> storage.setChunk(land, chunk));
    }

    public CompletableFuture<Void> claim(Player player, SChunk chunk, Land land, boolean verbose) {
        return future(() -> {
            try {
                if (getRemainingChunkCount(player).get() < 1) {
                    player.sendMessage("§cVous n'avez pas de tronçon disponible.");
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return;
            }

            if (getLandAt(chunk).equals(wilderness)) {
                claim(chunk, land);
                if (verbose) {
                    player.sendActionBar(Component.text("§a§lLe tronçon a bien été claim."));
                }
            } else if (verbose) {
                player.sendActionBar(Component.text("§c§lCe tronçon est déjà claim."));
            }
        });
    }

    public void claim(List<SChunk> chunks, Land land, Player player) {
        int TTChunks = chunks.size();
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!chunks.isEmpty()) {
                    claim(player, chunks.get(0), land, false);
                    chunks.remove(0);
                    if (chunks.size() % 50 == 0) {
                        int loadedChunks = TTChunks - chunks.size();
                        player.sendMessage("§aProtection des chunks... (" + loadedChunks + "/" + TTChunks + ") ["
                                + Math.round(loadedChunks * 100.0F / (TTChunks / 1.0F) * 10.0F) / 10.0F + "%]");
                    }
                } else {
                    player.sendMessage("§a§lLa selection a été protégée avec succès.");
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    public CompletableFuture<Void> unclaim(Player player, SChunk chunk, boolean verbose) {
        return future(() -> {
            Land land = getLandAt(chunk);
            if (land instanceof PlayerLand) {
                PlayerLand pland = (PlayerLand) land;
                if (pland.getOwner().equals(player.getUniqueId()) || plugin.isBypassing(player)) {
                    unclaim(chunk);
                    if (verbose) {
                        player.sendActionBar(Component.text("§a§lLe tronçon a bien été unclaim."));
                    }
                } else if (verbose) {
                    player.sendActionBar(Component.text("§c§lCe tronçon ne vous appartient pas !"));
                }
            } else if (verbose) {
                player.sendActionBar(Component.text("§c§lImpossible d'unclaim ce tronçon."));
            }
        });
    }

    public CompletableFuture<Void> unclaim(Player player, SChunk chunk, Land land, boolean verbose) {
        return future(() -> {
            Land l = getLandAt(chunk);
            if (l == null) {
                if (verbose)
                    player.sendActionBar(Component.text("§c§lCe tronçon n'est pas claim !"));
                return;
            }

            if (l.equals(land)) {
                unclaim(player, chunk, true);
            } else if (verbose) {
                player.sendActionBar(Component.text("§c§lImpossible d'unclaim ce tronçon, il n'appartient pas au territoire " + land.getName()));
            }
        });
    }

    public CompletableFuture<Void> unclaim(Chunk chunk) {
        return unclaim(new SChunk(chunk));
    }

    public CompletableFuture<Void> unclaim(SChunk schunk) {
        return future(() -> {
            Land land = getLandAt(schunk);
            if (land == null) {
                return;
            }
            chunks.remove(schunk);
            chunksCache.invalidate(schunk);
            storage.removeChunk(schunk);
        });
    }

    /*
     * FLAGS
     */

    public void addFlag(Land land, Flag flag) {
        land.getFlags().add(flag);
        future(() -> storage.addFlag(land, flag)).thenRun(() -> syncLand(land));
    }

    public void removeFlag(Land land, Flag flag) {
        land.getFlags().remove(flag);
        future(() -> storage.removeFlag(land, flag)).thenRun(() -> syncLand(land));
    }

    /*
     * BANS
     */

    public void ban(Player sender, Land land, UUID uuid) {
        if (sender.getUniqueId().equals(uuid)) {
            sender.sendMessage("§cVous ne pouvez pas vous ban vous même !");
            return;
        }
        if (land.isBanned(uuid)) {
            sender.sendMessage("§cCe joueur est déjà banni.");
            return;
        }
        land.getBans().add(uuid);
        future(() -> storage.addBan(land, uuid)).thenRun(() -> syncLand(land));
        sender.sendMessage("§aLe joueur a bien été banni.");

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            Land landat = getLandAt(player.getChunk());
            if (landat instanceof PlayerLand) {
                PlayerLand pland = (PlayerLand) landat;
                if (Objects.equals(pland.getOwner(), sender.getUniqueId())) {
                    player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
                }
            }
            player.sendMessage("§aVous avez été banni du territoire " + land.getName() + " par " + sender.getName() + ".");
        }
    }

    public void unban(Player sender, Land land, UUID uuid) {
        if (!land.isBanned(uuid)) {
            sender.sendMessage("§cCe joueur n'est pas banni.");
            return;
        }
        land.getBans().remove(uuid);
        future(() -> storage.removeBan(land, uuid)).thenRun(() -> syncLand(land));
        sender.sendMessage("§aLe joueur a bien été débanni.");
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage("§aVous avez été débanni du territoire " + land.getName() + " par " + sender.getName() + ".");
        }
    }

    /*
     * LINKS
     */

    public void addLink(Land land, Link link, Land with) {
        land.addLink(link, with);
        future(() -> storage.addLink(land, link, with)).thenRun(() -> syncLand(land));
    }

    public void removeLink(Land land, Link link) {
        land.removeLink(link);
        future(() -> storage.removeLink(land, link)).thenRun(() -> syncLand(land));
    }

    /*
     * SUBLANDS
     */

    public CompletableFuture<Void> createSublandAsync(Player player, Land superLand, String name) {
        return future(() -> {
            if (!superLand.getSubLands().isEmpty()) {
                for (SubLand subland : superLand.getSubLands().values()) {
                    if (subland.getName().equals(name)) {
                        player.sendMessage("§cIl y a déjà un territoire à ce nom.");
                        return;
                    }
                }
            }
            if (name.length() > 16) {
                player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
                return;
            }
            SubLand subLand = new SubLand(superLand, -1, name);
            subLand.setCuboid(new Cuboid(Bukkit.getWorlds().get(1), 0, 0, 0, 0, 0, 0), "non défini");
            storage.addLand(subLand);
            subLand.setId(storage.getLastId(LandType.SUBLAND));
            storage.setSubLandRegion(superLand, subLand);
            superLand.setSubLands(storage.getSubLands(superLand));
            player.sendMessage("§aLe sous-territoire au nom de " + name + " a été créée.");
        }).thenRun(() -> syncLand(superLand));
    }

    public CompletableFuture<Void> saveSubLandCuboid(SubLand subland) {
        return future(() -> storage.setSubLandRegion(subland.getSuperLand(), subland))
                .thenRun(() -> syncLand(subland.getSuperLand()));
    }


    public <T> CompletableFuture<T> future(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> future(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw (RuntimeException) e;
            }
        });
    }

    public LandMap getLandMap() {
        return landMap;
    }

    public void syncLand(Land land) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        if (plugin.getConfig().getBoolean("sync-enabled")) {
            core.getMessagingManager().sendMessage(LandsPlugin.SYNC_CHANNEL, String.valueOf(land.getId()));
        }
    }

    public void loadLand(int id) {
        future(() -> storage.getLand(id)).thenAccept(land -> {
            if (land == null && getLands().containsKey(id)) {
                getLands().remove(id);
            } else {
                getLands().put(land.getId(), land);
            }
        });
    }
}
