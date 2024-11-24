package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.utils.MobUtils;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageListeners implements Listener {

    private final LandRepository landRepository;

    public DamageListeners(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        Land land = landRepository.getLandAt(event.getEntity().getLocation());

        if (event.getEntity() instanceof Player) {
            if (land.hasFlag(Flag.INVINCIBLE)) {
                event.setCancelled(true);
                return;
            }
            if (land.hasFlag(Flag.PVP)) {
                event.setCancelled(false);
                return;
            }
        }

        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            Player player = getPlayerDamager(damageByEntityEvent);
            if (player != null) {
                if ((MobUtils.blockEntityList.contains(event.getEntityType()) && !land.isBypassing(player, Action.BLOCK_BREAK))
                        || (!(event.getEntity() instanceof Enemy) && !land.isBypassing(player, Action.PASSIVE_KILL))
                        || (event.getEntity().getCustomName() != null && !land.isBypassing(player, Action.TAGGED_KILL))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private Player getPlayerDamager(EntityDamageByEntityEvent event) {
        Player player = null;
        if (event.getCause() == DamageCause.PROJECTILE && event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }
        if (event.getCause() == DamageCause.ENTITY_EXPLOSION) {
            if (event.getDamager() instanceof Mob mob) {
                if (mob.getTarget() instanceof Player) {
                    player = (Player) mob.getTarget();
                }
            }
        }
        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        }
        return player;
    }
}
