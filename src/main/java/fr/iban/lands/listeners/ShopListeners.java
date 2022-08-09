package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.event.ShopPreCreateEvent;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopListeners implements Listener {
	
	private final LandManager landmanager;
	List<UUID> tempStaffs = new ArrayList<>();

	public ShopListeners(LandsPlugin landsPlugin) {
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

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void beforeCheck(PlayerInteractEvent e) {
		final Block b = e.getClickedBlock();

		if (b == null) {
			return;
		}

		if (!Util.canBeShop(b)) {
			return;
		}

		if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
			return; // Didn't right click it, we dont care.
		}

		final Shop shop = getShopPlayer(b.getLocation(), true);
		// Make sure they're not using the non-shop half of a double chest.
		if (shop == null) {
			return;
		}

		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();

		Land land = landmanager.getLandAt(b.getLocation());
		if(!land.isBypassing(player, Action.SHOP_OPEN)){
			return;
		}

		if(!shop.getModerator().isModerator(uuid)) {
			shop.getModerator().addStaff(uuid);
			tempStaffs.add(uuid);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void afterCheck(PlayerInteractEvent e) {
		final Block b = e.getClickedBlock();

		if (b == null) {
			return;
		}

		if (!Util.canBeShop(b)) {
			return;
		}

		if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
			return; // Didn't right click it, we dont care.
		}

		final Shop shop = getShopPlayer(b.getLocation(), true);
		// Make sure they're not using the non-shop half of a double chest.
		if (shop == null) {
			return;
		}

		UUID uuid = e.getPlayer().getUniqueId();

		if(tempStaffs.contains(uuid)) {
			shop.getModerator().delStaff(uuid);
			tempStaffs.remove(uuid);
		}
	}

	@Nullable
	public Shop getShopPlayer(@NotNull Location location, boolean includeAttached) {
		return includeAttached ? QuickShop.getInstance().getShopManager().getShopIncludeAttached(location, false) :
				QuickShop.getInstance().getShopManager().getShop(location);
	}

}
