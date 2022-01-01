package fr.iban.lands.menus;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class TrustEditMenu extends PaginatedMenu{

	private Land land;
	private LandManager manager;
	private UUID target;
	private TrustsManageMenu previousMenu;
	
	public TrustEditMenu(Player player, UUID target, Land land, LandManager manager) {
		super(player);
		this.target = target;
		this.land = land;
		this.manager = manager;
	}
	
	public TrustEditMenu(Player player, UUID target, Land land, LandManager manager, TrustsManageMenu previousMenu) {
		this(player, target, land, manager);
		this.previousMenu = previousMenu;
	}
	
	@Override
	public String getMenuName() {
		return "§2Permissions de " + Bukkit.getOfflinePlayer(target).getName();
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public int getElementAmount() {
		return Action.values().length;
	}
	
	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		
		checkBottonsClick(item, player);
		if(previousMenu != null && displayNameEquals(item, "§4Retour")) {
			previousMenu.open();
			return;
		}
		
		if(item.getItemMeta().getDisplayName().startsWith("§4")) {
			manager.addTrust(land, target, Action.getByDisplayName(item.getItemMeta().getDisplayName()));
		}else {
			manager.removeTrust(land, target, Action.getByDisplayName(item.getItemMeta().getDisplayName()));
		}
		
		super.open();
	}

	@Override
	public void setMenuItems() {
		addMenuBorder();

		for(int i = 0; i < getMaxItemsPerPage(); i++) {
			index = getMaxItemsPerPage() * page + i;
			if(index >= Action.values().length) break;
			Action action = Action.values()[index];
			if (action != null){
				if(land.isTrusted(target, action)) {
					inventory.addItem(new ItemBuilder(action.getItem()).setName("§2" + action.getDisplayName()).build());
				}else {
					inventory.addItem(new ItemBuilder(action.getItem()).setName("§4" + action.getDisplayName()).build());
				}
			}
		}
		
		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}
	}
	
	public LandManager getManager() {
		return manager;
	}

	public Land getLand() {
		return land;
	}
}
