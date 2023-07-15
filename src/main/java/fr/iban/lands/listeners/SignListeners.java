package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.land.Land;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class SignListeners implements Listener {

	private final LandManager landManager;

	public SignListeners(LandsPlugin plugin) {
		this.landManager = plugin.getLandManager();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player player = e.getPlayer();
		Land land = landManager.getLandAt(e.getBlock().getLocation());
		if(land != null && !land.isBypassing(player, Action.SIGN_EDIT)) {
			e.setCancelled(true);
		}
	}


}
