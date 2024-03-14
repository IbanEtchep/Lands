package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
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

public class BlockPlaceListener implements Listener {

    private final LandRepository landRepository;

    public BlockPlaceListener(LandsPlugin plugin) {
        this.landRepository = plugin.getLandRepository();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Land land = landRepository.getLandAt(block.getLocation());
        Material placedItem = event.getItemInHand().getType();

        if (event.getBlockAgainst().getType() == Material.LECTERN
                && (placedItem == Material.WRITABLE_BOOK || placedItem == Material.WRITTEN_BOOK)
                && land.isBypassing(player, Action.LECTERN_TAKE)) {
            return;
        }
        
        if (!land.isBypassing(player, Action.BLOCK_PLACE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlock();
        Land land = landRepository.getLandAt(block.getLocation());

        if (!land.isBypassing(event.getPlayer(), Action.BUCKET_EMPTY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlock();
        Land land = landRepository.getLandAt(block.getLocation());

        if (!land.isBypassing(event.getPlayer(), Action.BUCKET_FILL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player player) {
            Block block = event.getBlock();
            Land land = landRepository.getLandAt(block.getLocation());

            if (land != null && !land.isBypassing(player, Action.CAULDRON_FILL_EMPTY)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        if (event.getEntity() instanceof Player player) {
            Block block = event.getBlock();
            Land land = landRepository.getLandAt(block.getLocation());

            if (!land.isBypassing(player, Action.FROST_WALK)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        Dispenser dispenser = (Dispenser) block.getBlockData();
        Block targetBlock = event.getBlock().getRelative(dispenser.getFacing());

        Land dispenserLand = landRepository.getLandAt(block.getLocation());
        Land targetBlockLand = landRepository.getLandAt(targetBlock.getLocation());
        Land wilderness = landRepository.getWilderness();

        if (wilderness.equals(targetBlockLand)
                || dispenserLand == targetBlockLand
                || (dispenserLand.getOwner() != null
                && targetBlockLand.getOwner() != null
                && dispenserLand.getOwner().equals(targetBlockLand.getOwner()))) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Land fromLand = landRepository.getLandAt(event.getBlock().getLocation());
        Land toLand = landRepository.getLandAt(event.getToBlock().getLocation());
        Land wilderness = landRepository.getWilderness();

        if (wilderness.equals(toLand)
                || toLand.hasFlag(Flag.LIQUID_SPREAD)
                || fromLand == toLand
                || (toLand.getOwner() != null
                && fromLand.getOwner() != null
                && fromLand.getOwner().equals(toLand.getOwner()))) {
            return;
        }

        event.setCancelled(true);
    }
}
