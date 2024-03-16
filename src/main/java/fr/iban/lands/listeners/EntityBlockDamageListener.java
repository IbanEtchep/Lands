package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityBlockDamageListener implements Listener {

    private final LandRepository landRepository;

    public EntityBlockDamageListener(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler
    public void onEntityDamageBlock(EntityChangeBlockEvent e) {
        if (e.getEntityType() == EntityType.ENDERMAN
                || e.getEntityType() == EntityType.WITHER
                || e.getEntityType() == EntityType.ENDER_DRAGON) {
            Block block = e.getBlock();
            Land land = landRepository.getLandAt(block.getLocation());

            if (!land.hasFlag(Flag.BLOCK_DAMAGES_BY_ENTITY)) {
                e.setCancelled(true);
            }
        }
    }
}
