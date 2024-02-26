package fr.iban.lands.listeners;

import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.Message;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.land.Land;
import fr.iban.lands.land.SChunk;
import fr.iban.lands.utils.ChunkClaimSyncMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class LandSyncListener implements Listener {

    private final LandsPlugin plugin;
    private final LandManager landManager;

    public LandSyncListener(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landManager = plugin.getLandManager();
    }

    @EventHandler
    public void onSyncMessage(CoreMessageEvent e) {
        Message message = e.getMessage();

        if (message.getChannel().equals(LandsPlugin.LAND_SYNC_CHANNEL)) {
            landManager.loadLand(Integer.parseInt(message.getMessage()));
        }

        if (plugin.isMultipaperSupportEnabled()) {
            if (message.getChannel().equals(LandsPlugin.CHUNK_SYNC_CHANNEL)) {
                ChunkClaimSyncMessage claimSyncMessage = message.getMessage(ChunkClaimSyncMessage.class);
                Land land = landManager.getLandByID(claimSyncMessage.getId());
                SChunk sChunk = claimSyncMessage.getsChunk();
                if (sChunk.getServer().equals(plugin.getServerName())) {
                    if (claimSyncMessage.isUnclaim()) {
                        landManager.unclaim(sChunk, false);
                    } else {
                        landManager.claim(sChunk, land, false);
                    }
                }
            }

            if (message.getChannel().equals(LandsPlugin.BYPASS_SYNC_CHANNEL)) {
                UUID uuid = UUID.fromString(message.getMessage());
                plugin.setBypassing(uuid, !plugin.isBypassing(uuid));
            }

            if (message.getChannel().equals(LandsPlugin.DEBUG_SYNC_CHANNEL)) {
                UUID uuid = UUID.fromString(message.getMessage());
                plugin.setDebugging(uuid, !plugin.isInDebugMode(uuid));
            }
        }
    }
}
