package fr.iban.lands.listeners;

import fr.iban.lands.utils.Head;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HeadDatabaseListener implements Listener {
	
    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
    	Head.loadAPI();
    }

}
