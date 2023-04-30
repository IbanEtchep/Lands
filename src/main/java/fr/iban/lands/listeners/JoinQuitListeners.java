package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.land.Land;
import fr.iban.lands.utils.SeeChunks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JoinQuitListeners implements Listener {

    private final LandManager landManager;
    private final LandsPlugin plugin;

    public JoinQuitListeners(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landManager = plugin.getLandManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Land land = landManager.getLandAt(player.getLocation());
        if(land.hasFlag(Flag.INVISIBLE)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        SeeChunks seeChunks = landManager.getSeeChunks().get(player.getUniqueId());
        if(seeChunks != null) {
            seeChunks.stop();
            landManager.getSeeChunks().remove(player.getUniqueId());
        }
        if(player.isSilent()) {
            player.setSilent(false);
        }
        if(plugin.isInDebugMode(player)) {
            plugin.setDebugging(player.getUniqueId(), false);
        }
        if(plugin.isBypassing(player)) {
            plugin.setBypassing(player.getUniqueId(), false);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Land land = landManager.getLandAt(player.getLocation());
        if(land.hasFlag(Flag.INVISIBLE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        }
        plugin.getBypass().remove(player.getUniqueId());
        plugin.getDebugPlayers().remove(player.getUniqueId());
    }

}
