package fr.iban.lands.menus;

import fr.iban.lands.objects.SChunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.AreaSelector;

public class ClaimsManageMenu extends PaginatedMenu {

	private Land land;
	private LandManager manager;
	private LandManageMenu previousMenu;

	public ClaimsManageMenu(Player player, Land land, LandManager landManager, LandManageMenu previousMenu) {
		super(player);
		this.manager = landManager;
		this.land = land;
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName() + " §8> §2tronçons";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		ClickType clic = e.getClick();

		if(previousMenu != null && displayNameEquals(item, "§4Retour")) {
			previousMenu.open();
			return;
		}

		if(displayNameEquals(item, "§2Tronçon")) {
			if(clic == ClickType.LEFT) {
					manager.claim(player, new SChunk(player.getChunk()), land, true);
			}else if(clic == ClickType.RIGHT) {
				manager.unclaim(player, new SChunk(player.getChunk()), land, true);
			}
		}else if(displayNameEquals(item, "§2Selection")) {
			player.sendMessage("§2§lDébut de la sélection de la zone.");
			AreaSelector selector = new AreaSelector(player, land, manager, () -> {
				player.sendMessage("§cVous avez quitté le mode selection.");
				open();
			});
			player.closeInventory();
			selector.startSelecting();
		}else if(displayNameEquals(item, "§2Map")) {
			manager.getLandMap().display(player, land);
		}else if(displayNameEquals(item, "§2Liste des tronçons")) {
			new ChunkListMenu(player, manager, land, this).open();
		}

	}

	@Override
	public void setMenuItems() {
		inventory.setItem(10, new ItemBuilder(Material.PAPER).setDisplayName("§2Tronçon")
				.addLore("§aClic gauche : Ajouter le troçon où vous vous trouvez.")
				.addLore("§cClic droit : Retirer le tronçon où vous vous trouvez.")
				.addLore("§eVous pouvez aussi utiliser /land (un)claim <territoire>")
				.build());
		inventory.setItem(12, new ItemBuilder(Material.PAPER).setDisplayName("§2Selection")
				.addLore("§aOuvre le mode selection.")
				.build());

		inventory.setItem(14, new ItemBuilder(Material.MAP).setDisplayName("§2Map")
				.addLore("§aPermet de (un)claim à partir du chunk map.")
				.build());

		inventory.setItem(16, new ItemBuilder(Material.PAPER).setDisplayName("§2Liste des tronçons")
				.addLore("§aPermet de lister vos territoires")
				.build());
		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}
		fillWithGlass();
	}

}
