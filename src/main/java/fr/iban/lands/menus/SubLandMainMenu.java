package fr.iban.lands.menus;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.ConfirmMenu;
import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.SubLand;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.ItemBuilder;

public class SubLandMainMenu extends PaginatedMenu {

	private LandManager manager;
	private List<SubLand> lands;
	private LandsPlugin plugin;
	private Menu previousMenu;
	private Land superLand;

	public SubLandMainMenu(Player player, LandsPlugin plugin, List<SubLand> lands, Land superLand) {
		super(player);
		this.manager = plugin.getLandManager();
		this.lands = lands;
		this.plugin = plugin;
		this.superLand = superLand;
	}
	
	public SubLandMainMenu(Player player, LandsPlugin plugin, List<SubLand> lands, Land superLand, Menu previousMenu) {
		this(player, plugin, lands, superLand);
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§lSous-territoires de " + superLand.getName();
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
		
		if(previousMenu != null && displayNameEquals(current, "§4Retour")) {
			previousMenu.open();
		}

		checkBottonsClick(current, player);

		if(displayNameEquals(current, "§2Créer")) {
			CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
			player.closeInventory();
			player.sendMessage("§2§lVeuillez entrer le nom du territoire souhaité :");
			core.getTextInputs().put(player.getUniqueId(), texte -> {
				manager.createSublandAsync(player, superLand, texte).thenRun(() -> {
					Bukkit.getScheduler().runTask(plugin, () -> {
						new SubLandMainMenu(player, plugin, superLand.getSubLands().values().stream().collect(Collectors.toList()), superLand, previousMenu).open();
					});
				});
				core.getTextInputs().remove(player.getUniqueId());
			});
		}

		if(current.getType() == Material.PLAYER_HEAD) {
			Land land = getLandByName(current.getItemMeta().getDisplayName());

			if(land == null) {
				return;
			}

			ClickType click = e.getClick();
			if(click == ClickType.RIGHT) {
				new ConfirmMenu(player, "§cConfirmation de suppression", "§eVoulez-vous supprimer le territoire " + land.getName() + " ?", result -> {
					if(result) {
						manager.deleteLand(player, land);
						lands.remove(land);
					}
					super.open();
				}).open();
			}else if(click == ClickType.LEFT) {
				new LandManageMenu(player, plugin, manager, land, this).open();
			}

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

		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}

		inventory.setItem(35, new ItemBuilder(Head.OAK_PLUS.get()).setName("§2Créer").addLore("§aPermet de créer un nouveau territoire").build());
	}

	private ItemStack getLandItem(Land land) {
		return new ItemBuilder(Head.GRASS.get())
				.setDisplayName("§2" + land.getName())
				.addLore("§aClic gauche pour gérer ce territoire.")
				.addLore("§cClic droit pour supprimer ce territoire.")
				.addLore("")
				.addLore("§fTronçons : " + manager.getChunks(land).size())
				.addLore("§fJoueurs trust : " + land.getTrusts().size())
				.addLore("§fBannissements : " + land.getBans().size())
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
