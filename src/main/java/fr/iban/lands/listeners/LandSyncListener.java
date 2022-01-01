package fr.iban.lands.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.utils.LandSyncMessage;
import org.redisson.api.listener.MessageListener;

public class LandSyncListener implements MessageListener<LandSyncMessage> {

    private LandsPlugin plugin;

    public LandSyncListener(LandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, LandSyncMessage message) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        if(!core.getServerName().equals(message.getSenderServer())) {
            plugin.getLandManager().loadLand(message.getId());
        }
    }
}