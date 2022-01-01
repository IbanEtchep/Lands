package fr.iban.lands.menus;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.ConfirmMenu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.Head;

public class BansManageMenu extends PaginatedMenu {

	private Land land;
	private Set<UUID> bans;
	private LandManager manager;
	private LandManageMenu previousMenu;
	
	
	public BansManageMenu(Player player, LandManager manager, Land land) {
		super(player);
		this.manager = manager;
		this.land = land;
		this.bans = land.getBans();
	}

	public BansManageMenu(Player player, LandManager manager, Land land, LandManageMenu previousMenu) {
		this(player, manager, land);
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName() + " §8> §2bans";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		
		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}
		
		if(previousMenu != null && displayNameEquals(item, "§4Retour")) {
			previousMenu.open();
		}
		
		checkBottonsClick(item, player);
		
		if(item.getType() == Material.PLAYER_HEAD) {
			if(displayNameEquals(item, "§2Ajouter")) {
				CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
				player.closeInventory();
				player.sendMessage("§2§lVeuillez entrer le nom du joueur que vous voulez bannir. :");
				core.getTextInputs().put(player.getUniqueId(), texte -> {
					@SuppressWarnings("deprecation")
					OfflinePlayer target = Bukkit.getOfflinePlayer(texte);
					if(target != null) {
						manager.ban(player, land, target.getUniqueId());
					}else {
						player.sendMessage("§cCe joueur n'existe pas.");
						open();
					}
					core.getTextInputs().remove(player.getUniqueId());
				});
				return;
			}

			
			UUID uuid = getClickedTrustUUID(item.getItemMeta().getDisplayName());
			
			if(uuid == null) {
				return;
			}
			
			new ConfirmMenu(player, "§cConfirmation de débannissement", "§eVoulez-vous vraiment débannir " + Bukkit.getOfflinePlayer(uuid).getName() + " ?", result -> {
				if(result) {
					manager.unban(player, land, uuid);
				}
				super.open();
			}).open();
			
		}
		
	}

	@Override
	public void setMenuItems() {
		addMenuBorder();

		if(bans != null && !bans.isEmpty()) {
			
			int count = 0;
			for(UUID banned : bans) {
				index = getMaxItemsPerPage() * page + count;

				if(index <= bans.size() && count < maxItemsPerPage) {
					final int slot = inventory.firstEmpty();
					inventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("§cChargement...").build());
					getBannedItem(banned).thenAccept(item -> inventory.setItem(slot, item));
				}else {
					break;
				}
				
				count++;
				
			}
		}

		inventory.setItem(35, new ItemBuilder(Head.OAK_PLUS.get()).setName("§2Ajouter").addLore("§aPermet de bannir un joueur.").build());
	
		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}
	}

	@Override
	public int getElementAmount() {
		return bans.size();
	}
	
	private CompletableFuture<ItemStack> getBannedItem(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
	    	ItemStack head = new ItemStack(Material.PLAYER_HEAD);
	    	SkullMeta meta = (SkullMeta) head.getItemMeta();
	    	meta.setOwningPlayer(offlinePlayer);
	    	head.setItemMeta(meta);
			return new ItemBuilder(head).setDisplayName("§2§l"+offlinePlayer.getName())
					.addLore("§aClic pour débannir.")
					.build();
		});
	}
	
	private UUID getClickedTrustUUID(String itemdisplayname) {
		for(UUID banned : bans) {
			if(("§2§l" + Bukkit.getOfflinePlayer(banned).getName()).equals(itemdisplayname)){
				return banned;
			}
		}
		return null;
	}


}
