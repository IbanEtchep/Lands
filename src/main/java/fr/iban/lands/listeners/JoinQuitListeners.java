package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.utils.SeeChunks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JoinQuitListeners implements Listener {

    private final LandRepository landRepository;
    private final LandsPlugin plugin;

    public JoinQuitListeners(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landRepository = plugin.getLandRepository();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Land land = landRepository.getLandAt(player.getLocation());

        if (land.hasFlag(Flag.INVISIBLE)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        SeeChunks seeChunks = plugin.getSeeChunks().get(player.getUniqueId());

        if (seeChunks != null) {
            seeChunks.stop();
            plugin.getSeeChunks().remove(player.getUniqueId());
        }

        if (player.isSilent()) {
            player.setSilent(false);
        }

        if (plugin.isInDebugMode(player)) {
            plugin.setDebugging(player.getUniqueId(), false);
        }

        if (plugin.isBypassing(player)) {
            plugin.setBypassing(player.getUniqueId(), false);
        }

        plugin.runAsyncQueued(() -> {
            int defaultClaimLimit = plugin.getConfig().getInt("default-claim-limit", 1);
            int claimLimit = landRepository.getChunkLimit(player.getUniqueId());

            if(defaultClaimLimit > claimLimit) {
                landRepository.setChunkLimit(player.getUniqueId(), defaultClaimLimit);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Land land = landRepository.getLandAt(player.getLocation());
        if (land.hasFlag(Flag.INVISIBLE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        }

        plugin.getBypass().remove(player.getUniqueId());
        plugin.getDebugPlayers().remove(player.getUniqueId());
    }
}
