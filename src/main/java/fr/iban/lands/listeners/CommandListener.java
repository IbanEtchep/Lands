package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final LandRepository landRepository;

    public CommandListener(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (event.getMessage().toLowerCase().contains("sethome")) {
            Land land = landRepository.getLandAt(player.getLocation());
            if (land == null) return;

            if (land.isBypassing(player, Action.SET_HOME)) return;

            event.setCancelled(true);
        }
    }
}
