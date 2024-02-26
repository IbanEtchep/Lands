package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.land.Land;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class InteractListener implements Listener {

    private final LandManager landmanager;
    private final LandsPlugin plugin;
    private final List<Location> toClose = new ArrayList<>();

    public InteractListener(LandsPlugin landsPlugin) {
        this.plugin = landsPlugin;
        this.landmanager = landsPlugin.getLandManager();
    }

    @EventHandler
    public void onPhysics(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block == null) return;

        Land land = landmanager.getLandAt(block.getLocation());

        if (land == null) return;

        if (e.getAction() == org.bukkit.event.block.Action.PHYSICAL
                && block.getType() == Material.FARMLAND
                && !land.hasFlag(Flag.FARMLAND_GRIEF)) {
            e.setCancelled(true);
            return;
        }

        if (block.getType() == Material.ARMOR_STAND
                && !land.isBypassing(player, Action.ARMOR_STAND_INTERACT)) {
            e.setCancelled(true);
            return;
        }

        if (e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {

            if (land.hasFlag(Flag.DOORS_AUTOCLOSE)) {
                if (block.getBlockData() instanceof Openable openable) {
                    if (!land.isBypassing(e.getPlayer(), Action.ALL)
                            && !toClose.contains(block.getLocation())) {
                        boolean opened = openable.isOpen();
                        toClose.add(block.getLocation());
                        Bukkit.getScheduler()
                                .runTaskLater(
                                        plugin,
                                        () -> {
                                            Openable o = (Openable) block.getBlockData();
                                            o.setOpen(opened);
                                            block.setBlockData(o);
                                            block.getState().update();
                                            toClose.remove(block.getLocation());
                                        },
                                        60L);
                    }
                }
                return;
            }

            if ((block.getType() == Material.ANVIL && !land.isBypassing(player, Action.USE_ANVIL))
                    || (block.getType() == Material.BREWING_STAND
                    && !land.isBypassing(player, Action.BREWING_STAND_INTERACT))
                    || ((block.getBlockData() instanceof Powerable && block.getType() != Material.LECTERN)
                    && !land.isBypassing(player, Action.USE))
                    || (block.getBlockData() instanceof Bed && !land.isBypassing(player, Action.USE_BED))
                    || (block.getBlockData() instanceof RespawnAnchor
                    && !land.isBypassing(player, Action.USE_RESPAWN_ANCHOR))
                    || ((block.getType() == Material.FLOWER_POT
                    || block.getType().name().startsWith("POTTED_"))
                    && !land.isBypassing(player, Action.FLOWER_POT_INTERACT))
                    || (block.getType() == Material.DRAGON_EGG
                    && !land.isBypassing(player, Action.OTHER_INTERACTS))
                    || (((block.getState() instanceof InventoryHolder
                    && block.getType() != Material.LECTERN
                    && block.getType() != Material.BREWING_STAND)
                    || block.getType() == Material.JUKEBOX)
                    && !land.isBypassing(player, Action.OPEN_CONTAINER))
                    || (block.getType() == Material.LECTERN && !land.isBypassing(player, Action.LECTERN_READ))
                    || hasVehiculeInHand(player) && !land.isBypassing(player, Action.VEHICLE_PLACE_BREAK)
                    || hasArmorStandInHand(player) && !land.isBypassing(player, Action.BLOCK_PLACE)
                    || ((player.getInventory().getItemInMainHand().getType().toString().contains("SPAWN_EGG")
                    || player
                    .getInventory()
                    .getItemInOffHand()
                    .getType()
                    .toString()
                    .contains("SPAWN_EGG"))
                    && !land.isBypassing(player, Action.OTHER_INTERACTS))) {
                e.setCancelled(true);
            }

        } else if (e.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
            if (block.getType() == Material.DRAGON_EGG
                    && !land.isBypassing(player, Action.OTHER_INTERACTS)) {
                e.setCancelled(true);
            }
        } else if (e.getAction() == org.bukkit.event.block.Action.PHYSICAL) {
            if (block.getType() == Material.BIG_DRIPLEAF) {
                return;
            }
            if (!land.isBypassing(player, Action.PHYSICAL_INTERACT)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent e) {
        Block block = e.getBlock();
        Material material = block.getType();
        Land land = landmanager.getLandAt(block.getLocation());

        if (land != null && e.getEntityType() != EntityType.VILLAGER) {
            if (land.hasFlag(Flag.PRESSURE_PLATE_BY_ENTITY)) {
                switch (material) {
                    case ACACIA_PRESSURE_PLATE,
                            OAK_PRESSURE_PLATE,
                            DARK_OAK_PRESSURE_PLATE,
                            JUNGLE_PRESSURE_PLATE,
                            BIRCH_PRESSURE_PLATE,
                            CRIMSON_PRESSURE_PLATE,
                            WARPED_PRESSURE_PLATE,
                            STONE_PRESSURE_PLATE,
                            SPRUCE_PRESSURE_PLATE,
                            LIGHT_WEIGHTED_PRESSURE_PLATE,
                            POLISHED_BLACKSTONE_PRESSURE_PLATE -> {
                        return;
                    }
                }
            }
            if (land.hasFlag(Flag.TRIPWIRE_BY_ENTITY) && material == Material.TRIPWIRE) {
                return;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFerilize(BlockFertilizeEvent e) {
        Player player = e.getPlayer();

        if (player == null) return;

        Block block = e.getBlock();

        Land land = landmanager.getLandAt(block.getLocation());

        if (!land.isBypassing(player, Action.OTHER_INTERACTS)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractAtEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Land land = landmanager.getLandAt(e.getRightClicked().getLocation());

        if (land == null) {
            return;
        }

        if (!land.isBypassing(player, Action.ENTITY_INTERACT)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeash(PlayerLeashEntityEvent e) {
        Player player = e.getPlayer();
        Land land = landmanager.getLandAt(e.getEntity().getLocation());

        if (land == null) return;

        if (!land.isBypassing(player, Action.LEASH)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBeaconChange(PlayerChangeBeaconEffectEvent e) {
        if (e.getBeacon() == null) return;
        Player player = e.getPlayer();
        Land land = landmanager.getLandAt(e.getBeacon().getLocation());

        if (!land.isBypassing(player, Action.CHANGE_BEACON_EFFECT)) {
            e.setCancelled(true);
        }
    }

    private boolean hasVehiculeInHand(Player player) {
        return player.getInventory().getItemInMainHand().getType().toString().contains("BOAT")
                || player.getInventory().getItemInMainHand().getType().toString().contains("MINECART")
                || player.getInventory().getItemInOffHand().getType().toString().contains("BOAT")
                || player.getInventory().getItemInOffHand().getType().toString().contains("MINECART");
    }

    private boolean hasArmorStandInHand(Player player) {
        return player.getInventory().getItemInMainHand().getType() == Material.ARMOR_STAND
                || player.getInventory().getItemInOffHand().getType() == Material.ARMOR_STAND;
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        Player player = e.getPlayer();
        Land land = landmanager.getLandAt(e.getRightClicked().getLocation());

        if (land == null) {
            return;
        }

        if (!land.isBypassing(player, Action.ARMOR_STAND_INTERACT)) {
            e.setCancelled(true);
        }
    }
}
