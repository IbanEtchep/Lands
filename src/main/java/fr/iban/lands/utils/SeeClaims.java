package fr.iban.lands.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.GuildLand;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.model.land.PlayerLand;
import fr.iban.lands.model.land.SystemLand;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class SeeClaims {

    private final LandsPlugin plugin;
    private final LandRepository landRepository;
    private final Player player;
    private Location lastPlayerLocation;
    private WrappedTask task;
    private final Set<Location> lastBlockLocations = new HashSet<>();

    public SeeClaims(Player player, LandsPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.landRepository = plugin.getLandRepository();
    }

    public void showWalls() {
        task = plugin.getScheduler().runTimerAsync(() -> {
            if (!player.isOnline()) {
                stop();
                return;
            }

            if (lastPlayerLocation != null && lastPlayerLocation.distanceSquared(player.getLocation()) < 1) {
                return;
            }

            lastPlayerLocation = player.getLocation();

            Set<Location> newLocations = new HashSet<>();
            var wallBlocks = getWallBlocks();
            int py = (int) player.getLocation().getY();

            // Afficher les nouveaux blocs
            for (Map.Entry<Location, StateType> wallBlockEntry : wallBlocks.entrySet()) {
                for (int y = Math.max(0, py - 30); y < Math.min(255, py + 30); y++) {
                    Location blockLoc = wallBlockEntry.getKey().clone();
                    blockLoc.setY(y);

                    // Ne pas afficher si trop proche du joueur
                    if (player.getLocation().distanceSquared(blockLoc) < 9
                            || blockLoc.getBlock().getType() != Material.AIR) {
                        continue;
                    }

                    newLocations.add(blockLoc);
                    WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(
                            new Vector3i(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ()),
                            WrappedBlockState.getDefaultState(wallBlockEntry.getValue()).getGlobalId()
                    );
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
                }
            }

            // Retirer les anciens blocs
            lastBlockLocations.stream()
                    .filter(loc -> !newLocations.contains(loc))
                    .forEach(loc -> {
                        WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(
                                new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
                                SpigotConversionUtil.fromBukkitBlockData(loc.getBlock().getBlockData()).getGlobalId()
                        );
                        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
                    });

            lastBlockLocations.clear();
            lastBlockLocations.addAll(newLocations);

        },  10L, 5L);
    }


    private Map<Location, StateType> getWallBlocks() {
        Map<Location, StateType> locations = new HashMap<>();
        SChunk playerChunk = new SChunk(player.getLocation());
        int viewDistance = 5;

        for (int x = -viewDistance; x <= viewDistance; x++) {
            for (int z = -viewDistance; z <= viewDistance; z++) {
                SChunk chunk = playerChunk.getRelativeChunk(x, z);

                Land land = landRepository.getLandAt(chunk);
                if (land == null || landRepository.isWilderness(land)) {
                    continue;
                }

                int baseY = 0;

                if (shouldShowWall(land, chunk.getNorthChunk())) {
                    for (Location loc : chunk.getNorthWall(baseY)) {
                        locations.put(loc, getLandStateType(player, land));
                    }
                }
                if (shouldShowWall(land, chunk.getSouthChunk())) {
                    for (Location loc : chunk.getSouthWall(baseY)) {
                        locations.put(loc, getLandStateType(player, land));
                    }
                }
                if (shouldShowWall(land, chunk.getEastChunk())) {
                    for (Location loc : chunk.getEastWall(baseY)) {
                        locations.put(loc, getLandStateType(player, land));
                    }
                }
                if (shouldShowWall(land, chunk.getWestChunk())) {
                    for (Location loc : chunk.getWestWall(baseY)) {
                        locations.put(loc, getLandStateType(player, land));
                    }
                }
            }
        }
        return locations;
    }

    public StateType getLandStateType(Player player, Land land) {
        if(land instanceof PlayerLand pland && pland.getOwner().equals(player.getUniqueId())) {
            return StateTypes.LIME_STAINED_GLASS;
        }

        if (land instanceof SystemLand) {
            return StateTypes.RED_STAINED_GLASS;
        }

        if (land instanceof GuildLand gland && gland.isGuildMember(player.getUniqueId())) {
            return StateTypes.LIGHT_BLUE_STAINED_GLASS;
        }

        return StateTypes.WHITE_STAINED_GLASS;
    }

    private boolean shouldShowWall(Land currentLand, SChunk adjacent) {
        Land adjacentLand = landRepository.getLandAt(adjacent);
        return currentLand != adjacentLand;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        // Restaurer tous les blocs
        for (Location loc : lastBlockLocations) {
            WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(
                    new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
                    SpigotConversionUtil.fromBukkitBlockData(loc.getBlock().getBlockData()).getGlobalId()
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
        lastBlockLocations.clear();
    }

    public void forceUpdate() {
        lastPlayerLocation = null;
    }
}
