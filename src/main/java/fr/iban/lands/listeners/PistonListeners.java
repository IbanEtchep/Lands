package fr.iban.lands.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;

public class PistonListeners implements Listener {

	private LandManager landmanager;

	public PistonListeners(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onPiston(BlockPistonExtendEvent e) {
		Land pistonLand= landmanager.getLandAt(e.getBlock().getLocation());

		for(Block block : e.getBlocks()) {
			Land land = landmanager.getLandAt(block.getRelative(e.getDirection()).getLocation());

			if(land.isWilderness() || land == pistonLand || (land.getOwner() != null && pistonLand.getOwner() != null && land.getOwner().equals(pistonLand.getOwner())))
				continue;

			e.setCancelled(true);
		}

	}

}
