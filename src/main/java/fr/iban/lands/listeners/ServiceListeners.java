package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

public class ServiceListeners implements Listener {

    private LandsPlugin plugin;

    public ServiceListeners(LandsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (!event.getProvider().getService().getName().equals("net.milkbowl.vault.economy.Economy")) {
            return;
        }
        plugin.setupEconomy();
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (!event.getProvider().getService().getName().equals("net.milkbowl.vault.economy.Economy")) {
            return;
        }
        plugin.setupEconomy();
    }
}
