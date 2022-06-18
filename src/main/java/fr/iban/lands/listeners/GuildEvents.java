package fr.iban.lands.listeners;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
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

    @EventHandler
    public void onPartyDelete(BukkitPartiesPartyPostDeleteEvent e) {
        landManager.getGuildLandsAsync(e.getParty().getId()).thenAccept(lands -> {
            lands.forEach(land -> landManager.deleteLand(land));
        });
    }

}
