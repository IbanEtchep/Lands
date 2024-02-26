package fr.iban.lands.listeners;

import fr.iban.bukkitcore.event.PlayerPreTeleportEvent;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.events.LandEnterEvent;
import fr.iban.lands.land.Land;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private LandManager manager;
    private LandsPlugin plugin;

    public TeleportListener(LandsPlugin plugin) {
        this.manager = plugin.getLandManager();
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        Land lto = manager.getLandAt(to);
        Land lfrom = manager.getLandAt(from);
        Player player = e.getPlayer();

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
                && !lto.isBypassing(player, Action.CHORUS_TELEPORT)) {
            e.setCancelled(true);
            return;
        }

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                && !lto.isBypassing(player, Action.ENDER_PEARL_TELEPORT)) {
            e.setCancelled(true);
            return;
        }

        LandEnterEvent enter = new LandEnterEvent(player, lfrom, lto);
        Bukkit.getPluginManager().callEvent(enter);
        if (enter.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCoreTeleport(PlayerPreTeleportEvent e) {
        Player player = e.getPlayer();

        if (player != null) {
            Land land = manager.getLandAt(player.getLocation());

            if (land.hasFlag(Flag.INSTANT_TELEPORT)) {
                e.setDelay(0);
            }
        }
    }
}
