package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class HangingListeners implements Listener {

    private final LandRepository landRepository;

    public HangingListeners(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        Player player = getPlayerRemover(e);

        if (player == null) {
            return;
        }

        Land land = landRepository.getLandAt(e.getEntity().getLocation());

        if (!land.isBypassing(player, Action.BLOCK_BREAK)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingPlaceEvent e) {
        Player player = e.getPlayer();

        if (player == null) {
            return;
        }

        Land land = landRepository.getLandAt(e.getEntity().getLocation());

        if (!land.isBypassing(player, Action.BLOCK_PLACE)) {
            e.setCancelled(true);
        }
    }

    private Player getPlayerRemover(HangingBreakByEntityEvent event) {
        Player player = null;
        if (event.getCause() == RemoveCause.ENTITY && event.getRemover() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                player = shooter;
            }
        }
        if (event.getCause() == RemoveCause.EXPLOSION) {
            if (event.getRemover() instanceof Mob mob) {
                if (mob.getTarget() instanceof Player mobTarget) {
                    player = mobTarget;
                }
            }
        }
        if (event.getRemover() instanceof Player remover) {
            player = remover;
        }
        return player;
    }
}
