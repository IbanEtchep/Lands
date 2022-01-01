package fr.iban.lands.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;

public class EntityBlockDamageListener implements Listener {

	private LandManager landmanager;

	public EntityBlockDamageListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onEntityDamageBlock(EntityChangeBlockEvent e) {
		if(e.getEntityType() == EntityType.ENDERMAN || e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.ENDER_DRAGON) {
			Block block = e.getBlock();
			Land land = landmanager.getLandAt(block.getLocation());

			if(land == null) 
				return;

			if(!land.hasFlag(Flag.BLOCK_DAMAGES_BY_ENTITY)) {
				e.setCancelled(true);
			}
		}

	}

}
