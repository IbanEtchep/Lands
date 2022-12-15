package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GuildEvents implements Listener {

    private LandsPlugin plugin;
    private LandManager landManager;

    public GuildEvents(LandsPlugin plugin) {
        this.plugin = plugin;
        this.landManager = plugin.getLandManager();
    }

}
