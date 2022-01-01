package fr.iban.lands.listeners;

import java.util.Set;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.MobUtils;

public class DamageListeners implements Listener {


	private LandManager landmanager;
	private Set<EntityType> mobs = MobUtils.mobsList;

	public DamageListeners(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(EntityDamageEvent e) {
		Land land = landmanager.getLandAt(e.getEntity().getLocation());

		if(land == null) {
			return;
		}

		if(e.getEntity() instanceof Player) {
			if(land.hasFlag(Flag.INVINCIBLE)) {
				e.setCancelled(true);
				return;
			}
			if(land.hasFlag(Flag.PVP)) {
				e.setCancelled(false);
				return;
			}
		}


		if(e instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)e;
			Player player = getPlayerDamager(event);
			if(player != null) {
				if((e.getEntityType() == EntityType.ITEM_FRAME && !land.isBypassing(player, Action.BLOCK_BREAK))
						|| (!mobs.contains(e.getEntityType()) && !land.isBypassing(player, Action.PASSIVE_KILL))
						|| (e.getEntity().getCustomName() != null && !land.isBypassing(player, Action.TAGGED_KILL))) {
					e.setCancelled(true);
				}
			}
		}
	}

	private Player getPlayerDamager(EntityDamageByEntityEvent event) {
		Player player = null;
		if(event.getCause() == DamageCause.PROJECTILE && event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getDamager();
			if(projectile.getShooter() instanceof Player) {
				player = (Player)projectile.getShooter();
			}
		}
		if(event.getCause() == DamageCause.ENTITY_EXPLOSION) {
			if(event.getDamager() instanceof Mob) {
				Mob mob = (Mob)event.getDamager();
				if(mob.getTarget() instanceof Player) {
					player = (Player)mob.getTarget();
				}
			}
		}
		if(event.getDamager() instanceof Player) {
			player = (Player) event.getDamager();
		}
		return player;
	}
}
