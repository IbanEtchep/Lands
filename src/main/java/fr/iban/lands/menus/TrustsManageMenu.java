package fr.iban.lands.menus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.lands.LandManager;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.Trust;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.ItemBuilder;

public class TrustsManageMenu extends PaginatedMenu {

	private Land land;
	private Map<UUID, Trust> trusts;
	private LandManager manager;
	private LandManageMenu previousMenu;
	private Map<Integer, UUID> uuidAtSlot = new HashMap<>();


	public TrustsManageMenu(Player player, LandManager manager, Land land) {
		super(player);
		this.manager = manager;
		this.land = land;
		this.trusts = land.getTrusts();
	}

	public TrustsManageMenu(Player player, LandManager manager, Land land, LandManageMenu previousMenu) {
		this(player, manager, land);
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName() + " §8> §2permissions";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();

		checkBottonsClick(item, player);

		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}

		if(previousMenu != null && displayNameEquals(item, "§4Retour")) {
			previousMenu.open();
		}

		if(item.getType() == Material.PLAYER_HEAD) {
			if(displayNameEquals(item, "§2Ajouter")) {
				player.closeInventory();
				player.sendMessage("§2§lVeuillez entrer le nom du joueur à qui vous voulez modifier les permissions. :");
				CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
				core.getTextInputs().put(player.getUniqueId(), texte -> {
					Player target = Bukkit.getPlayer(texte);
					if(target == null) {
						player.sendMessage("§cCe joueur n'est pas en ligne.");
						open();
					}else {
						new TrustEditMenu(player, target.getUniqueId(), land, manager, this).open();
					}
					core.getTextInputs().remove(player.getUniqueId());
				});
				return;
			}

			if(displayNameEquals(item, "§2Permissions globales")) {
				new GlobalTrustEditMenu(player, land, manager, this).open();
				return;
			}

			UUID uuid = uuidAtSlot.get(e.getSlot());

			if(uuid == null) {
				return;
			}

			new TrustEditMenu(player, uuid, land, manager, this).open();
		}

	}

	@Override
	public void setMenuItems() {
		uuidAtSlot.clear();
		addMenuBorder();

		if(trusts != null && !trusts.isEmpty()) {
			List<UUID> uuids = trusts.keySet().stream().collect(Collectors.toList());
			for(int i = 0; i < getMaxItemsPerPage(); i++) {
				index = getMaxItemsPerPage() * page + i;
				if(index >= uuids.size()) break;
				if (uuids.get(index) != null){
					final int slot = inventory.firstEmpty();
					UUID warp = uuids.get(index);
					uuidAtSlot.put(slot, warp);
					inventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("§cChargement...").build());
					getTrustItem(uuids.get(index)).thenAccept(item -> inventory.setItem(slot, item));
				}

			}

		}
		inventory.setItem(27, new ItemBuilder(Head.GLOBE.get()).setName("§2Permissions globales").addLore("§aPermet de définir des permissions qui seront appliquées à tout le monde.").build());

		inventory.setItem(35, new ItemBuilder(Head.OAK_PLUS.get()).setName("§2Ajouter").addLore("§aPermet d'ajouter un joueur pour éditer ses permissions.").build());

		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}
	}

	@Override
	public int getElementAmount() {
		return trusts.size();
	}

	private CompletableFuture<ItemStack> getTrustItem(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(offlinePlayer);
			head.setItemMeta(meta);
			return new ItemBuilder(head).setDisplayName("§2§l"+offlinePlayer.getName())
					.addLore("§aClic gauche pour éditer")
					.addLore("§cClic droit pour supprimer")
					.build();
		});
	}


}
