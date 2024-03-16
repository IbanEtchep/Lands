package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class FireListener implements Listener {

    private final LandRepository landRepository;

    public FireListener(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Land land = landRepository.getLandAt(block.getLocation());

        if (event.getSource().getType() == Material.FIRE && !land.hasFlag(Flag.FIRE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockBurnEvent event) {
        Block block = event.getBlock();
        Land land = landRepository.getLandAt(block.getLocation());

        if (!land.hasFlag(Flag.FIRE)) {
            event.setCancelled(true);
        }
    }
}
