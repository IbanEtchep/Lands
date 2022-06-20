package fr.iban.lands.listeners;

import com.google.gson.Gson;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.Message;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LandSyncListener implements Listener {

    private final LandManager landManager;

    public LandSyncListener(LandManager landManager) {
        this.landManager = landManager;
    }


    @EventHandler
    public void onSyncMessage(CoreMessageEvent e) {
        Message message = e.getMessage();

        if(!message.getChannel().equals(LandsPlugin.SYNC_CHANNEL)){
            return;
        }

        landManager.loadLand(Integer.parseInt(e.getMessage().getMessage()));
    }
}