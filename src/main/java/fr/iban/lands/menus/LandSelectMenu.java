package fr.iban.lands.menus;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.LandSelectCallback;

public class LandSelectMenu extends PaginatedMenu {

	private List<Land> lands;
	private LandSelectCallback callback;

	public LandSelectMenu(Player player, List<Land> lands, LandSelectCallback callback) {
		super(player);
		this.lands = lands;
		this.callback = callback;
	}

	@Override
	public String getMenuName() {
		return "§2Selectionnez un territoire :";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public int getElementAmount() {
		return lands.size();
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack current = e.getCurrentItem();

		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}
		
		if(displayNameEquals(current, "§4Annuler")) {
			callback.cancel();
			return;
		}

		checkBottonsClick(current, player);


		if(current.getType() == Material.PLAYER_HEAD) {
			Land land = getLandByName(current.getItemMeta().getDisplayName());

			if(land == null) {
				return;
			}
			
			callback.select(land);

		}


	}

	@Override
	public void setMenuItems() {
		addMenuBorder();

		if(lands != null && !lands.isEmpty()) {
			for(int i = 0; i < getMaxItemsPerPage(); i++) {
				index = getMaxItemsPerPage() * page + i;
				if(index >= lands.size()) break;
				if (lands.get(index) != null){
					Land land = lands.get(index);
					inventory.addItem(getLandItem(land));
				}
			}
		}
		
		inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Annuler")
				.addLore("§cRetourner au menu précédent")
				.build());

	}

	private ItemStack getLandItem(Land land) {
		return new ItemBuilder(Head.GRASS.get())
				.setDisplayName("§2" + land.getName())
				.addLore("§aCliquez pour selectionner ce territoire")
				.build();
	}

	private Land getLandByName(String string) {
		for (Land land : lands) {
			if(string.equals("§2"+land.getName())) {
				return land;
			}
		}
		return null;
	}

}
