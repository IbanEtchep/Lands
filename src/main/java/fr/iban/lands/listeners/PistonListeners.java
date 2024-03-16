package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.model.land.Land;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonListeners implements Listener {

    private final LandRepository landRepository;

    public PistonListeners(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        Land pistonLand = landRepository.getLandAt(event.getBlock().getLocation());

        for (Block block : event.getBlocks()) {
            Land land = landRepository.getLandAt(block.getRelative(event.getDirection()).getLocation());
            Land wilderness = landRepository.getWilderness();

            if (land.equals(wilderness)
                    || land == pistonLand
                    || (land.getOwner() != null
                    && pistonLand.getOwner() != null
                    && land.getOwner().equals(pistonLand.getOwner()))) continue;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Land pistonLand = landRepository.getLandAt(event.getBlock().getLocation());

        for (Block block : event.getBlocks()) {
            Land land = landRepository.getLandAt(block.getRelative(event.getDirection().getOppositeFace()).getLocation());

            if (land.equals(landRepository.getWilderness())
                    || land == pistonLand
                    || (land.getOwner() != null
                    && pistonLand.getOwner() != null
                    && land.getOwner().equals(pistonLand.getOwner()))) continue;

            event.setCancelled(true);
        }
    }
}
