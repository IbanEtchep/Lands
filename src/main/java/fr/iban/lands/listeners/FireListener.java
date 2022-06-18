package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class FireListener implements Listener {

	private LandManager landmanager;

	public FireListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onFireSpread(BlockSpreadEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());

		if (e.getSource().getType() == Material.FIRE && !land.hasFlag(Flag.FIRE)) {
			e.setCancelled(true);
		}

	}

	@EventHandler
	public void onFireSpread(BlockBurnEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());

		if (!land.hasFlag(Flag.FIRE)) {
			e.setCancelled(true);
		}

	}

}
