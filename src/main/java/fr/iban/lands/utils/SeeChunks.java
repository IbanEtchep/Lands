package fr.iban.lands.utils;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class SeeChunks {

    private final Collection<BukkitTask> tasks = new ArrayList<>();
    private final Player player;
    private final LandManager manager;

    public SeeChunks(Player player, LandManager manager) {
        this.player = player;
        this.manager = manager;
    }

    public void showParticles() {

        tasks.add((new BukkitRunnable() {
            @Override
            public void run() {
                int py = (int) player.getLocation().getY();
				if(!player.isOnline()) return;
                getPointsAsync().thenAccept(points -> {
                    for (Location loc : getPoints()) {
                        for (int y = (py - 10 >= 0 ? py - 30 : 0); y < (py + 10 <= 255 ? py + 30 : 255); y++) {
                            showParticle(loc, y);
                        }
                    }
                });
            }
        }).runTaskTimer(LandsPlugin.getInstance(), 1L, 20L));
    }

    public void stop() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }

    private Collection<Location> getPoints() {
        Collection<Location> points = new ArrayList<>();
        Collection<Chunk> chunks = getChunksAroundChunk(player.getChunk());
        for (Chunk chunk : chunks.toArray(new Chunk[0])) {
            for (Chunk c : getChunksAroundChunk(chunk)) {
                if (!chunks.contains(c)) {
                    chunks.add(c);
                }
            }
        }
        for (Chunk chunk : chunks) {
            points.addAll(getCorners(chunk));
        }
        return points;
    }

    public CompletableFuture<Collection<Location>> getPointsAsync() {
        return manager.future(this::getPoints);
    }

    private Collection<Location> getCorners(Chunk chunk) {
        Collection<Location> corners = new ArrayList<>();
        Land land = manager.getLandAt(chunk);
        Location nordwest = chunk.getBlock(0, 0, 0).getLocation().clone();
        if (!manager.getLandAt(nordwest.clone().add(0, 0, -1).getChunk()).equals(land) || !manager.getLandAt(nordwest.clone().add(-1, 0, 0).getChunk()).equals(land)) {
            corners.add(nordwest);
        }
        Location sudest = chunk.getBlock(15, 0, 15).getLocation().add(1, 0, 1).clone();
        if (!manager.getLandAt(sudest.clone().add(0, 0, 1).getChunk()).equals(land) || !manager.getLandAt(sudest.clone().add(1, 0, 0).getChunk()).equals(land)) {
            corners.add(sudest);
        }
        Location sudwest = new Location(chunk.getWorld(), nordwest.getX(), 0, sudest.getZ());
        if (!manager.getLandAt(sudwest.clone().add(0, 0, 1).getChunk()).equals(land) || !manager.getLandAt(sudwest.clone().add(-1, 0, 0).getChunk()).equals(land)) {
            corners.add(sudwest);
        }
        Location nordest = new Location(chunk.getWorld(), sudest.getX(), 0, nordwest.getZ());
        if (!manager.getLandAt(nordest.clone().add(0, 0, -1).getChunk()).equals(land) || !manager.getLandAt(nordest.clone().add(1, 0, 0).getChunk()).equals(land)) {
            corners.add(nordest);
        }

        if (corners.contains(sudwest) && corners.contains(nordwest)) {
            Location mid = new Location(chunk.getWorld(), (sudwest.getX() + nordwest.getX()) / 2, 0, (sudwest.getZ() + nordwest.getZ()) / 2);
            if (!land.equals(manager.getLandAt(mid.clone().add(-1, 0, 0).getChunk()))) {
                corners.add(mid);
            }

        }

        if (corners.contains(nordest) && corners.contains(sudest)) {
            Location mid = new Location(chunk.getWorld(), (sudest.getX() + nordest.getX()) / 2, 0, (sudest.getZ() + nordest.getZ()) / 2);
            if (!land.equals(manager.getLandAt(mid.clone().add(1, 0, 0).getChunk()))) {
                corners.add(mid);
            }
        }

        if (corners.contains(nordwest) && corners.contains(nordest)) {
            Location mid = new Location(chunk.getWorld(), (nordest.getX() + nordwest.getX()) / 2, 0, (nordest.getZ() + nordwest.getZ()) / 2);
            if (!land.equals(manager.getLandAt(mid.clone().add(0, 0, -1).getChunk()))) {
                corners.add(mid);
            }
        }

        if (corners.contains(sudest) && corners.contains(sudwest)) {
            Location mid = new Location(chunk.getWorld(), (sudest.getX() + sudwest.getX()) / 2, 0, (sudest.getZ() + sudwest.getZ()) / 2);
            if (!land.equals(manager.getLandAt(mid.clone().add(0, 0, 1).getChunk()))) {
                corners.add(mid);
            }
        }

        return corners;
    }

    public Collection<Chunk> getChunksAroundChunk(Chunk chunk) {
        int[] offset = {-1, 0, 1};

        int baseX = chunk.getX();
        int baseZ = chunk.getZ();

        Collection<Chunk> chunksAroundPlayer = new HashSet<>();
        for (int x : offset) {
            for (int z : offset) {
                Chunk c = chunk.getWorld().getChunkAt(baseX + x, baseZ + z);
                chunksAroundPlayer.add(c);
            }
        }
        return chunksAroundPlayer;
    }

    private void showParticle(Location loc, int y) {
        loc.setY(y);
        player.spawnParticle(Particle.FALLING_OBSIDIAN_TEAR, loc, 1);
    }

}
