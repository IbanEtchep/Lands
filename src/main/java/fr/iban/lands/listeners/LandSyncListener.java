package fr.iban.lands.listeners;

import com.google.gson.Gson;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.Message;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LandSyncListener implements Listener {

    private LandManager landManager;
    private Gson gson = new Gson();

    public LandSyncListener(LandManager plugin) {
        this.landManager = landManager;
    }


    @EventHandler
    public void onSyncMessage(CoreMessageEvent e) {
        Message message = e.getMessage();

        if(!message.getChannel().equals(landManager.SYNC_CHANNEL)){
            return;
        }

        landManager.loadLand(Integer.parseInt(e.getMessage().getMessage()));
    }
}