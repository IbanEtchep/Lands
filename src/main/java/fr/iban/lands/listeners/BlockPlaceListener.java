package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {


    private LandManager landmanager;

    public BlockPlaceListener(LandsPlugin landsPlugin) {
        this.landmanager = landsPlugin.getLandManager();
    }


    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Land land = landmanager.getLandAt(block.getLocation());
        Material placedItem = e.getItemInHand().getType();

        if (land != null) {
            if (e.getBlockAgainst().getType() == Material.LECTERN
                    && (placedItem == Material.WRITABLE_BOOK || placedItem == Material.WRITTEN_BOOK)
                    && land.isBypassing(player, Action.LECTERN_TAKE)) {
                return;
            }
            if (!land.isBypassing(player, Action.BLOCK_PLACE)) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
        Block block = e.getBlock();
        Land land = landmanager.getLandAt(block.getLocation());

        if (land != null && !land.isBypassing(e.getPlayer(), Action.BUCKET_EMPTY)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent e) {
        Block block = e.getBlock();
        Land land = landmanager.getLandAt(block.getLocation());

        if (land != null && !land.isBypassing(e.getPlayer(), Action.BUCKET_FILL)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            Block block = e.getBlock();
            Land land = landmanager.getLandAt(block.getLocation());

            if (land != null && !land.isBypassing(player, Action.CAULDRON_FILL_EMPTY)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Block block = e.getBlock();
            Land land = landmanager.getLandAt(block.getLocation());

            if (!land.isBypassing(player, Action.FROST_WALK)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent e) {
        Block block = e.getBlock();
        Dispenser dispenser = (Dispenser) block.getBlockData();
        Block targetBlock = e.getBlock().getRelative(dispenser.getFacing());

        Land dispenserLand = landmanager.getLandAt(block.getLocation());
        Land targetBlockLand = landmanager.getLandAt(targetBlock.getLocation());

        if (targetBlockLand.isWilderness() || dispenserLand == targetBlockLand || (dispenserLand.getOwner() != null && targetBlockLand.getOwner() != null && dispenserLand.getOwner().equals(targetBlockLand.getOwner()))) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        Land fromLand = landmanager.getLandAt(e.getBlock().getLocation());
        Land toLand = landmanager.getLandAt(e.getToBlock().getLocation());

        if (toLand.isWilderness() || toLand.hasFlag(Flag.LIQUID_SPREAD) || fromLand == toLand
                || (toLand.getOwner() != null && fromLand.getOwner() != null && fromLand.getOwner().equals(toLand.getOwner()))) {
            return;
        }

        e.setCancelled(true);
    }
}
