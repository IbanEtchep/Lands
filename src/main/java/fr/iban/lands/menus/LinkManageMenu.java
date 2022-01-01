package fr.iban.lands.menus;

import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Link;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.ItemBuilder;
import fr.iban.lands.utils.LandSelectCallback;

public class LinkManageMenu extends Menu {

	private Land land;
	private Menu previousMenu;
	private LandManager manager;

	public LinkManageMenu(Player player, LandManager manager, Land land, Menu previousMenu) {
		super(player);
		this.land = land;
		this.manager = manager;
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName() + " §8> §2liens";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if(previousMenu != null && displayNameEquals(item, "§4Retour")) {
			previousMenu.open();
		}

		if(displayNameEquals(item, "§2Lien permissions globales")) {
			clickAction(Link.GLOBALTRUST);
		}else if(displayNameEquals(item, "§2Lien permissions joueurs")) {
			clickAction(Link.TRUSTS);
		}else if(displayNameEquals(item, "§2Lien bannissements")) {
			clickAction(Link.BANS);
		}
	}

	private void clickAction(Link link) {
		if(land.getLinkedLand(link) == null) {
			new LandSelectMenu(player, manager.getLands(player).stream().filter(l -> !l.equals(land) && !(l.getLinkedLand(link) != null && l.getLinkedLand(link).equals(land))).collect(Collectors.toList()), new LandSelectCallback() {

				@Override
				public void select(Land selected) {

					manager.addLink(land, link, selected);

					open();
				}

				@Override
				public void cancel() {
					open();
				}
			}).open();

		}else {
			manager.removeLink(land, link);
			open();
		}
	}

	@Override
	public void setMenuItems() {
		Land globalTrustLink = land.getLinkedLand(Link.GLOBALTRUST);
		Land trustLink = land.getLinkedLand(Link.TRUSTS);
		Land banLink = land.getLinkedLand(Link.BANS);

		inventory.setItem(10, new ItemBuilder(Head.GLOBE.get()).setDisplayName("§2Lien permissions globales")
				.addLore("§aPermet de faire en sorte que les permissions globales")
				.addLore("§adu territoire soient celles d'un autre territoire.")
				.addLore("")
				.addLore("§f§lActuellement : " + (globalTrustLink == null ? "§c§lAucun" : "§a§l" + globalTrustLink.getName()))
				.build());
		inventory.setItem(13, new ItemBuilder(Head.HAL.get()).setDisplayName("§2Lien permissions joueurs")
				.addLore("§aPermet de faire en sorte que les permissions joueur")
				.addLore("§adu territoire soient celles d'un autre territoire.")
				.addLore("")
				.addLore("§f§lActuellement : " + (trustLink == null ? "§c§lAucun" : "§a§l" + trustLink.getName()))
				.build());
		inventory.setItem(16, new ItemBuilder(Head.NO_ENTRY.get()).setDisplayName("§2Lien bannissements")
				.addLore("§aPermet de faire en sorte que les bannissements")
				.addLore("§adu territoire soient ceux d'un autre territoire.")
				.addLore("")
				.addLore("§f§lActuellement : " + (banLink == null ? "§c§lAucun" : "§a§l" + banLink.getName()))
				.build());
		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}
		fillWithGlass();
	}

}
