package fr.iban.lands.menus;

import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.SubLand;
import fr.iban.lands.utils.Cuboid;
import fr.iban.lands.utils.CuboidSelector;
import fr.iban.lands.utils.Head;

public class LandManageMenu extends Menu {

	private Land land;
	private LandManager manager;
	private Menu previousMenu;
	private LandsPlugin plugin;

	public LandManageMenu(Player player, LandsPlugin plugin, LandManager manager, Land land) {
		super(player);
		this.land = land;
		this.manager = manager;
		this.plugin = plugin;
	}

	public LandManageMenu(Player player, LandsPlugin plugin, LandManager manager, Land land, Menu previousMenu) {
		this(player, plugin, manager, land);
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName();
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack current = e.getCurrentItem();

		if(previousMenu != null && current.getType() == Material.RED_STAINED_GLASS_PANE) {
			previousMenu.open();
		}
		
		if(displayNameEquals(current, "§2Permissions")) {
			new TrustsManageMenu(player, manager, land, this).open();
		}else if(displayNameEquals(current, "§2Tronçons protégés")) {
			new ClaimsManageMenu(player, land, manager, this).open();
		}else if(displayNameEquals(current, "§2Paramètres")) {
			new LandSettingsMenu(player, land, manager, this).open();
		}else if(displayNameEquals(current, "§2Bannissements")) {
			new BansManageMenu(player, manager, land, this).open();
		}else if(displayNameEquals(current, "§2Sous-territoires")) {
			new SubLandMainMenu(player, plugin, land.getSubLands().values().stream().collect(Collectors.toList()), land, previousMenu).open();;
		}else if(displayNameEquals(current, "§2Zone protégée :")) {
			player.sendMessage("§2§lDébut de la sélection de la zone.");
			CuboidSelector selector = new CuboidSelector(player, (SubLand)land, manager, () -> {
				player.sendMessage("§cVous avez quitté le mode selection.");
				open();
			});
			player.closeInventory();
			selector.startSelecting();
		}

	}

	@Override
	public void setMenuItems() {
		if(land.getType() != LandType.SUBLAND) {
			inventory.setItem(10, new ItemBuilder(Head.CHEST_DIRT.get()).setDisplayName("§2Tronçons protégés")
					.addLore("§aPermet de gérer les tronçons que le territoire inclut.")
					.build());
		}else {
			SubLand subland = (SubLand)land;
			Cuboid cuboid = subland.getCuboid();
			if(cuboid != null) {
				inventory.setItem(10, new ItemBuilder(Head.CHEST_DIRT.get()).setDisplayName("§2Zone protégée :")
						.addLore("§7Serveur : " + subland.getServer())
						.addLore(String.format("§7Monde : x: %s", cuboid.getWorld().getName()))
						.addLore(String.format("§7Position 1 : x: %d y: %d z:%d", cuboid.getUpperX(), cuboid.getUpperY(), cuboid.getUpperZ()))
						.addLore(String.format("§7Position 2 : x: %d y: %d z:%d", cuboid.getLowerX(), cuboid.getLowerY(), cuboid.getLowerZ()))
						.addLore("§aCliquez pour redéfinir la zone protégée.")
						.build());
			}else if(subland.getServer() != null){
				inventory.setItem(10, new ItemBuilder(Head.CHEST_DIRT.get()).setDisplayName("§2Zone protégée :")
						.addLore("§cDéfini sur le serveur : " + subland.getServer())
						.addLore("§aCliquez pour définir la zone protégée.")
						.build());
			}else {
				inventory.setItem(10, new ItemBuilder(Head.CHEST_DIRT.get()).setDisplayName("§2Zone protégée :")
						.addLore("§cNon définie")
						.addLore("§aCliquez pour définir la zone protégée.")
						.build());
			}
		}
		inventory.setItem(12, new ItemBuilder(Head.HAL.get()).setDisplayName("§2Permissions")
				.addLore("§aPermet de gérer les permissions du territoire.")
				.build());
		inventory.setItem(14, new ItemBuilder(Head.FIREBALL.get()).setDisplayName("§2Paramètres")
				.addLore("§aPermet de modifier les paramètres du territoire")
				.build());
		inventory.setItem(16, new ItemBuilder(Head.NO_ENTRY.get()).setDisplayName("§2Bannissements")
				.addLore("§aPermet de gérer les bannissements.")
				.build());
		
		if(land.getType() != LandType.SUBLAND && player.hasPermission("lands.sublands")) {
			inventory.setItem(27, new ItemBuilder(Head.DIRT_S.get()).setDisplayName("§2Sous-territoires")
					.addLore("§aPermet de gérer les sous-territoires.")
					.addLore("§7Ils servent à définir des territoires cuboïdes à")
					.addLore("§7l'intérieur des chunks protégés par le territoire.")
					.build());
		}
		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu")
					.build());
		}
		fillWithGlass();
	}

}
