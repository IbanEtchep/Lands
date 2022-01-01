package fr.iban.lands.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class DropListener implements Listener {
	
	private LandManager landmanager;

	public DropListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		Land land = landmanager.getLandAt(player.getLocation());
		
		if(land == null) return;
		
		if(!land.isBypassing(player, Action.DROP)) {
			e.setCancelled(true);
		}
	}

}
