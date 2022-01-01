package fr.iban.lands.listeners;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class HangingListeners implements Listener {

	private LandManager landmanager;

	public HangingListeners(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e) {	
		
		Player player = getPlayerRemover(e);
		
		if(player != null) {			
			
			Land land = landmanager.getLandAt(e.getEntity().getLocation());

			if(land != null) {
				if(land.isBypassing(player, Action.BLOCK_BREAK)) {
					return;
				}
				e.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onHangingBreak(HangingPlaceEvent e) {	
		Player player = e.getPlayer();
		Land land = landmanager.getLandAt(e.getEntity().getLocation());


		if(land != null && !land.isBypassing(player, Action.BLOCK_PLACE)) {
			e.setCancelled(true);
		}

	}

	private Player getPlayerRemover(HangingBreakByEntityEvent event) {
		Player player = null;
		if(event.getCause() == RemoveCause.ENTITY && event.getRemover() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getRemover();
			if(projectile.getShooter() instanceof Player) {
				player = (Player)projectile.getShooter();
			}
		}
		if(event.getCause() == RemoveCause.EXPLOSION) {
			if(event.getRemover() instanceof Mob) {
				Mob mob = (Mob)event.getRemover();
				if(mob.getTarget() instanceof Player) {
					player = (Player)mob.getTarget();
				}
			}
		}
		if(event.getRemover() instanceof Player) {
			player = (Player) event.getRemover();
		}
		return player;
	}
}
