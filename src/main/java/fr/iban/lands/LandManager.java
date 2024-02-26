package fr.iban.lands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.land.*;
import fr.iban.lands.storage.AbstractStorage;
import fr.iban.lands.storage.SqlStorage;
import fr.iban.lands.utils.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
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
    private final Map<String, Land> worldsDefaultLands = new ConcurrentHashMap<>();
    private final Map<UUID, SeeChunks> seeChunks = new HashMap<>();
    private final LandMap landMap;
    private final LandsPlugin plugin;
    private SystemLand wilderness = new SystemLand(-1, "Zone sauvage");
    private final Cache<SChunk, Land> chunksCache =
            Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(1000).build();

    public LandManager(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landMap = new LandMap(this);
        storage = new SqlStorage();
        loadData();
    }

    /*
     * Charge les données depuis la bdd
     */
    public void loadData() {
        final long start = System.currentTimeMillis();
        plugin.getLogger().log(Level.INFO, "Chargement des données :.");
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        plugin,
                        () -> {
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
                            getSystemLand("Zone sauvage")
                                    .thenAccept(
                                            wild -> {
                                                if (wild == null) {
                                                    saveWilderness(wilderness);
                                                } else {
                                                    wilderness = wild;
                                                }
                                            });
                            storage
                                    .getWorldsDefaultLands()
                                    .forEach(
                                            (world, landId) -> {
                                                Land land = getLandByID(landId);
                                                if (land != null) {
                                                    worldsDefaultLands.put(world, getLandByID(landId));
                                                }
                                            }
                                    );
                            loaded = true;
                            plugin
                                    .getLogger()
                                    .info(
                                            "Chargement des données terminé en "
                                                    + (System.currentTimeMillis() - start)
                                                    + " ms.");
                            startPaymentCheckTask();
                        });
    }

    public @Nullable Land getLandByID(int id) {
        return lands.get(id);
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

    // Retourne le territoire du nom donné pour le joueur donné.
    public CompletableFuture<PlayerLand> getPlayerLand(Player player, String name) {
        return future(
                () -> {
                    for (PlayerLand land : getLands(player)) {
                        if (land.getName().equalsIgnoreCase(name)) {
                            return land;
                        }
                    }
                    return null;
                });
    }

    // Retourne le territoire du nom donné pour le joueur donné.
    public CompletableFuture<SystemLand> getSystemLand(String name) {
        return future(
                () -> {
                    for (SystemLand land : getSystemLands()) {
                        if (land.getName().equalsIgnoreCase(name)) {
                            return land;
                        }
                    }
                    return null;
                });
    }

    // Retourne le territoire du nom donné pour le la guilde donnée.
    public CompletableFuture<GuildLand> getGuildLand(UUID guildID, String name) {
        return future(
                () -> {
                    for (GuildLand land : getGuildLands(guildID)) {
                        if (land.getName().equalsIgnoreCase(name)) {
                            return land;
                        }
                    }
                    return null;
                });
    }

    /*
    Utilisé pour les commandes, pour auto completer les noms des territoires (joueur, guild, system) que le joueur a
    la permission de gérer.
     */
    public List<String> getManageableLandsNames(Player player) {
        List<String> landNames = new ArrayList<>();
        for (PlayerLand land : getLands(player)) {
            landNames.add(land.getName());
        }

        if (plugin.isGuildsHookEnabled()
                && plugin.getGuildDataAccess().canManageGuildLand(player.getUniqueId())) {
            UUID guildID = plugin.getGuildDataAccess().getGuildId(player.getUniqueId());
            if (guildID != null) {
                for (GuildLand land :
                        getGuildLands(plugin.getGuildDataAccess().getGuildId(player.getUniqueId()))) {
                    landNames.add("guild:" + land.getName());
                }
            }
        }

        if (plugin.isBypassing(player)) {
            for (SystemLand land : getSystemLands()) {
                landNames.add("system:" + land.getName());
            }
        }
        return landNames;
    }

    /*
    Utilisé pour les commandes, pour vérifier que l'entrée correspond bien à un territoire que le joueur peut gérer.
     */
    public Land getManageableLandFromName(Player player, String name) {
        if (name.startsWith("system:") && player.hasPermission("lands.bypass")) {
            return getSystemLands().stream()
                    .filter(land -> land.getName().equals(name.split(":")[1]))
                    .findFirst()
                    .orElse(null);
        }
        if (name.startsWith("guild:")
                && plugin.getGuildDataAccess() != null
                && plugin.getGuildDataAccess().canManageGuildLand(player.getUniqueId())) {
            return getGuildLands(plugin.getGuildDataAccess().getGuildId(player.getUniqueId())).stream()
                    .filter(land -> land.getName().equals(name.split(":")[1]))
                    .findFirst()
                    .orElse(null);
        }
        return getLands(player).stream()
                .filter(land -> land.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean canManageLand(Player player, Land land) {
        if (land instanceof GuildLand guildLand) {
            return plugin.getGuildDataAccess() != null
                    && plugin.getGuildDataAccess().canManageGuildLand(player.getUniqueId())
                    && getGuildLands(plugin.getGuildDataAccess().getGuildId(player.getUniqueId()))
                    .contains(guildLand);
        } else if (land instanceof PlayerLand playerLand) {
            return playerLand.getOwner().equals(player.getUniqueId());
        } else if (land instanceof SystemLand systemLand) {
            return player.hasPermission("lands.bypass");
        }
        return false;
    }

    // Retourne le territoire du nom donné pour le joueur donné.
    public CompletableFuture<PlayerLand> getPlayerFirstLand(Player player) {
        return future(
                () -> {
                    List<PlayerLand> plands = getLands(player);
                    if (!plands.isEmpty()) {
                        return plands.get(0);
                    }
                    player.sendMessage("§cVous n'avez pas de territoire, créez-en un depuis le menu /lands.");
                    return null;
                });
    }

    /*
     * Permet de créer un nouveau territoire en tant que joueur.
     */
    public CompletableFuture<PlayerLand> createLand(Player player, String name) {
        return future(
                () -> {
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
                    player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
                    syncLand(land);

                    return land;
                });
    }

    /*
     * Permet de créer un nouveau territoire en tant que joueur.
     */
    public CompletableFuture<GuildLand> createGuildLand(Player player, String name) {
        return future(
                () -> {
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
                    land.setCreatedAt(new Date());
                    player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
                    syncLand(land);

                    return land;
                });
    }

    /*
     * Permet de créer un nouveau territoire système.
     */
    public CompletableFuture<SystemLand> createSystemLand(Player player, String name) {
        return future(
                () -> {
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
                    land.setCreatedAt(new Date());
                    land.setBans(new HashSet<>());
                    land.setFlags(new HashSet<>());
                    player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
                    syncLand(land);
                    return land;
                });
    }

    public void saveWilderness(SystemLand land) {
        future(
                () -> {
                    getLands().put(-1, land);
                    storage.addLand(land);
                });
    }

    //    /*
    //     * Permet de créer un nouveau territoire.
    //     */
    //    public CompletableFuture<PlayerLand> createLand(UUID uuid, String name) {
    //        return future(() -> {
    //            PlayerLand land = new PlayerLand(-1, uuid, name);
    //            storage.addLand(land);
    //            int id = storage.getLandID(land.getType(), land.getOwner(), land.getName());
    //            land.setId(id);
    //            getLands().put(id, land);
    //            land.setBans(new HashSet<>());
    //            land.setFlags(new HashSet<>());
    //            Bukkit.broadcast(Component.text("§aLe territoire au nom de " + name + " pour " +
    // Bukkit.getOfflinePlayer(uuid).getName() + " a été créée."));
    //            syncLand(land);
    //            return land;
    //        });
    //    }

    /*
     * Permet de supprimer un territoire
     */
    public CompletableFuture<Void> deleteLand(Land land) {
        return future(
                () -> {
                    if (land == null) {
                        return;
                    }
                    storage.deleteLand(land);
                    getChunks(land)
                            .forEach(
                                    schunk -> {
                                        getChunks().remove(schunk);
                                        chunksCache.invalidate(schunk);
                                    });

                    if (land.hasSubLand()) {
                        for (SubLand subland : land.getSubLands().values()) {
                            deleteLand(subland);
                        }
                    }
                    worldsDefaultLands.forEach(
                            (world, defaultLand) -> {
                                if (defaultLand.equals(land)) {
                                    worldsDefaultLands.remove(world);
                                }
                            });
                    syncLand(land);
                    getLands().remove(land.getId());
                });
    }

    /*
     * Permet de renommer un territoire
     */
    public CompletableFuture<Void> renameLand(Land land, Player player, String name) {
        return future(
                () -> {
                    if (getPlayerLand(player, name).join() != null) {
                        player.sendMessage("§cVous avez déjà un territoire à ce nom.");
                        return;
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
    public CompletableFuture<Integer> getChunkCount(UUID uuid) {
        return future(() -> storage.getChunkCount(uuid));
    }

    public CompletableFuture<Integer> getMaxChunkCount(UUID uuid) {
        return future(
                () -> {
                    if (plugin.isBypassing(uuid)) {
                        return 1000000;
                    }

                    return storage.getChunkLimit(uuid);
                });
    }

    public CompletableFuture<Integer> getRemainingChunkCount(UUID uuid) {
        return future(() -> getMaxChunkCount(uuid).get() - getChunkCount(uuid).get());
    }

    /*
     * Retourne la liste des chunks d'un territoire.
     */
    public Collection<SChunk> getChunks(Land land) {
        return getChunks().entrySet().stream()
                .filter(entry -> entry.getValue().equals(land))
                .map(Entry::getKey)
                .toList();
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

    public Land getLandAt(Chunk chunk) {
        return getLandAt(new SChunk(chunk));
    }

    public Land getLandAt(SChunk schunk) {
        if (!loaded) {
            return wilderness;
        }
        Land defaultLand = worldsDefaultLands.getOrDefault(schunk.getWorld(), wilderness);
        return chunksCache.get(schunk, land -> getChunks().getOrDefault(schunk, defaultLand));
    }

    public Land getLandAt(Location loc) {
        Land land = getLandAt(new SChunk(loc));
        SubLand subLand = land.getSubLandAt(loc);
        return Objects.requireNonNullElse(subLand, land);
    }

    /*
     * Ajouter un chunk à un territoire :
     */
    public void claim(SChunk chunk, Land land, boolean persist) {
        chunk.setCreatedAt(new Date());
        getChunks().put(chunk, land);
        chunksCache.invalidate(chunk);
        if (persist) {
            syncChunk(land, chunk, false);
            future(() -> storage.setChunk(land, chunk));
        }
    }

    public void claim(SChunk chunk, Land land) {
        claim(chunk, land, true);
    }

    public boolean hasEnouphClaims(Player player, Land land) {
        UUID landOwner = land.getOwner();

        if (landOwner == null || plugin.isBypassing(player)) {
            return true;
        }

        int remaining = getRemainingChunkCount(landOwner).join();

        if (land instanceof GuildLand && remaining <= 0) {
            remaining = getRemainingChunkCount(player.getUniqueId()).join();
        }

        return remaining > 0;
    }

    public CompletableFuture<Void> claim(Player player, SChunk chunk, Land land, boolean verbose) {
        return future(
                () -> {
                    if (!hasEnouphClaims(player, land)) {
                        player.sendMessage("§cVous n'avez plus de claim disponible.");
                        return;
                    }

                    if (getLandAt(chunk).equals(wilderness)) {
                        if (getRemainingChunkCount(land.getOwner()).join() <= 0) {
                            giveClaims(player, land.getOwner(), 1);
                        }

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
                        player.sendMessage(
                                "§aProtection des chunks... ("
                                        + loadedChunks
                                        + "/"
                                        + TTChunks
                                        + ") ["
                                        + Math.round(loadedChunks * 100.0F / (TTChunks) * 10.0F) / 10.0F
                                        + "%]");
                    }
                } else {
                    player.sendMessage("§a§lLa selection a été protégée avec succès.");
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    public void unclaim(Player player, SChunk chunk, boolean verbose) {
        future(
                () -> {
                    Land land = getLandAt(chunk);
                    if (canManageLand(player, land) || plugin.isBypassing(player)) {
                        unclaim(chunk);
                        if (verbose) {
                            player.sendActionBar(Component.text("§a§lLe tronçon a bien été unclaim."));
                        }
                    } else if (verbose) {
                        player.sendActionBar(Component.text("§c§lCe tronçon ne vous appartient pas !"));
                    }
                });
    }

    public CompletableFuture<Void> unclaim(Player player, SChunk chunk, Land land, boolean verbose) {
        return future(
                () -> {
                    Land l = getLandAt(chunk);
                    if (l == null) {
                        if (verbose) player.sendActionBar(Component.text("§c§lCe tronçon n'est pas claim !"));
                        return;
                    }

                    if (l.equals(land)) {
                        unclaim(player, chunk, true);
                    } else if (verbose) {
                        player.sendActionBar(Component.text("§c§lImpossible d'unclaim ce tronçon, il n'appartient pas au territoire " + land.getName()));
                    }
                });
    }

    public void unclaim(Chunk chunk) {
        unclaim(new SChunk(chunk), true);
    }

    public void unclaim(SChunk schunk) {
        unclaim(schunk, true);
    }

    public void unclaim(SChunk schunk, boolean persist) {
        future(
                () -> {
                    Land land = getLandAt(schunk);
                    if (land == null) {
                        return;
                    }
                    chunks.remove(schunk);
                    chunksCache.invalidate(schunk);
                    if (persist) {
                        syncChunk(land, schunk, true);
                        storage.removeChunk(schunk);
                    }
                });
    }

    public void setDefaultWorldLand(World world, Land land) {
        worldsDefaultLands.put(world.getName(), land);
        storage.setWorldDefaultLand(world.getName(), land);
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
            if (landat instanceof PlayerLand pland) {
                if (Objects.equals(pland.getOwner(), sender.getUniqueId())) {
                    player.teleportAsync(
                            plugin
                                    .getConfig()
                                    .getLocation(
                                            "spawn-location",
                                            Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation()));
                }
            }
            player.sendMessage(
                    "§aVous avez été banni du territoire "
                            + land.getName()
                            + " par "
                            + sender.getName()
                            + ".");
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
        return future(
                () -> {
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
                    subLand.setCuboid(
                            new Cuboid(Bukkit.getWorlds().get(1), 0, 0, 0, 0, 0, 0), "non défini");
                    storage.addLand(subLand);
                    subLand.setId(storage.getLastId(LandType.SUBLAND));
                    storage.setSubLandRegion(superLand, subLand);
                    superLand.setSubLands(storage.getSubLands(superLand));
                    player.sendMessage("§aLe sous-territoire au nom de " + name + " a été créée.");
                })
                .thenRun(() -> syncLand(superLand));
    }

    public void saveSubLandCuboid(SubLand subland) {
        future(() -> storage.setSubLandRegion(subland.getSuperLand(), subland))
                .thenRun(() -> syncLand(subland.getSuperLand()));
    }

    public LandMap getLandMap() {
        return landMap;
    }

    public Map<UUID, SeeChunks> getSeeChunks() {
        return seeChunks;
    }

    public void syncLand(Land land) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        if (plugin.getConfig().getBoolean("sync-enabled")) {
            core.getMessagingManager()
                    .sendMessage(LandsPlugin.LAND_SYNC_CHANNEL, String.valueOf(land.getId()));
        }
    }

    public void syncChunk(Land land, SChunk chunk, boolean unclaim) {
        if (plugin.isMultipaperSupportEnabled()) {
            CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
            ChunkClaimSyncMessage message = new ChunkClaimSyncMessage(land.getId(), chunk, unclaim);
            core.getMessagingManager().sendMessage(LandsPlugin.CHUNK_SYNC_CHANNEL, message);
        }
    }

    public void loadLand(int id) {
        future(() -> storage.getLand(id))
                .thenAccept(
                        land -> {
                            // Clear old land cache
                            Land cachedLand = getLandByID(id);
                            if (cachedLand != null) {
                                getChunks(cachedLand).forEach(chunksCache::invalidate);
                                if (land == null) {
                                    getChunks(cachedLand).forEach(schunk -> getChunks().remove(schunk));
                                    getLands().remove(id);
                                } else {
                                    getChunks(cachedLand).forEach(schunk -> getChunks().put(schunk, land));
                                }
                            }

                            if (land != null) {
                                getLands().put(land.getId(), land);
                            }
                        });
    }

    public void startPaymentCheckTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::handleAllPayments, 10L, 72000L);
    }

    public void handleAllPayments() {
        lands.values().forEach(this::handlePayment);
    }

    public boolean handlePayment(Land land) {
        if (plugin.isGuildsHookEnabled()) {
            AbstractGuildDataAccess guildDataAccess = plugin.getGuildDataAccess();
            if (land instanceof GuildLand guildLand) {
                if (guildLand.getNextPaiement() != null
                        && LocalDateTime.now()
                        .isAfter(DateUtils.convertToLocalDateTime(land.getLastPayment()).plusWeeks(1))) {
                    boolean success =
                            guildDataAccess.withdraw(
                                    guildLand.getGuildId(),
                                    guildLand.getTotalWeeklyPrice(),
                                    "payer les frais de vos territoires");
                    if (success) {
                        guildLand.setLastPayment(new Date());
                        guildLand.setPaymentDue(false);
                        storage.updateLandLastPaymentDate(land);
                        return true;
                    } else {
                        guildLand.setPaymentDue(true);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void increaseChunkLimit(UUID uuid, int amount) {
        future(() -> storage.increaseChunkLimit(uuid, amount));
    }

    public void decreaseChunkLimit(UUID uuid, int amount) {
        future(() -> storage.decreaseChunkLimit(uuid, amount));
    }

    public <T> CompletableFuture<T> future(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(
                () -> {
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
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        throw (RuntimeException) e;
                    }
                }
        );
    }

    public void giveClaims(Player player, UUID target, int amount) {
        future(() -> {
            int remaining = getRemainingChunkCount(player.getUniqueId()).join();

            if (remaining < amount) {
                player.sendMessage("§cVous n'avez pas assez de claims.");
                return;
            }

            storage.decreaseChunkLimit(player.getUniqueId(), amount);
            storage.increaseChunkLimit(target, amount);

            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                player.sendMessage("§aVous avez donné " + amount + " claims à " + targetPlayer.getName() + ".");
                targetPlayer.sendMessage("§aVous avez reçu " + amount + " claims de " + player.getName() + ".");
            } else {
                player.sendMessage("§aVous avez donné " + amount + " claims.");
            }
        });
    }

    public void transferClaims(UUID from, UUID to) {
        future(() -> {
            int maxChunkCount = getMaxChunkCount(from).join();
            storage.increaseChunkLimit(to, maxChunkCount);
            storage.setChunkLimit(from, 0);
        });
    }
}
