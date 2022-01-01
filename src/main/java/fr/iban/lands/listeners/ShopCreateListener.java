package fr.iban.lands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.event.ShopPreCreateEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class ShopCreateListener implements Listener {
	
	private LandManager landmanager;

	public ShopCreateListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onShopCreate(ShopPreCreateEvent e) {
		Land land = landmanager.getLandAt(e.getLocation());
		if(land == null)
			return;
		
		if(!land.isBypassing(e.getPlayer(), Action.SHOP_CREATE)) {
			e.setCancelled(true);
		}
	}

}
