package fr.iban.lands.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class CommandListener implements Listener {
	
	private LandManager landmanager;

	public CommandListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();

		if(e.getMessage().toLowerCase().contains("sethome")) {
			Land land = landmanager.getLandAt(player.getLocation());
			if(land == null)
				return;
			
			if(land.isBypassing(player, Action.SET_HOME))
				return;
			
			e.setCancelled(true);
		}
	}

}
