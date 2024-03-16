package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class PlayerTakeLecternBookListener implements Listener {

    private final LandRepository landRepository;

    public PlayerTakeLecternBookListener(LandsPlugin plugin) {
        this.landRepository = plugin.getLandRepository();
    }

    @EventHandler
    public void onTakeBook(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        Land land = landRepository.getLandAt(event.getLectern().getLocation());

        if (!land.isBypassing(player, Action.LECTERN_TAKE)) {
            event.setCancelled(true);
        }
    }
}
