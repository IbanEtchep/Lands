package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.LinkType;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.ItemBuilder;
import fr.iban.lands.utils.LandSelectCallback;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LinkManageMenu extends Menu {

    private final Land land;
    private final Menu previousMenu;
    private final LandsPlugin plugin;
    private final LandRepository landRepository;

    public LinkManageMenu(Player player, LandsPlugin plugin, Land land, Menu previousMenu) {
        super(player);
        this.land = land;
        this.plugin = plugin;
        this.landRepository = plugin.getLandRepository();
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

        if (item == null || item.getType().isAir()) return;

        if (previousMenu != null && displayNameEquals(item, "§4Retour")) {
            previousMenu.open();
        }

        if (displayNameEquals(item, "§2Lien permissions globales")) {
            clickAction(LinkType.GLOBALTRUST);
        } else if (displayNameEquals(item, "§2Lien permissions joueurs")) {
            clickAction(LinkType.TRUSTS);
        } else if (displayNameEquals(item, "§2Lien bannissements")) {
            clickAction(LinkType.BANS);
        }
    }

    private void clickAction(LinkType link) {
        if (land.getLinkedLand(link) == null) {
            List<Land> lands = landRepository.getLands(player.getUniqueId()).stream()
                    .filter(l -> !l.equals(land) && !(l.getLinkedLand(link) != null && l.getLinkedLand(link).equals(land)))
                    .toList();

            new LandSelectMenu(player, lands,
                    new LandSelectCallback() {

                        @Override
                        public void select(Land selected) {

                            landRepository.addLink(land, link, selected);

                            open();
                        }

                        @Override
                        public void cancel() {
                            open();
                        }
                    })
                    .open();

        } else {
            landRepository.removeLink(land, link);
            open();
        }
    }

    @Override
    public void setMenuItems() {
        Land globalTrustLink = land.getLinkedLand(LinkType.GLOBALTRUST);
        Land trustLink = land.getLinkedLand(LinkType.TRUSTS);
        Land banLink = land.getLinkedLand(LinkType.BANS);

        inventory.setItem(10, new ItemBuilder(Head.GLOBE.get())
                .setDisplayName("§2Lien permissions globales")
                .addLore("§aPermet de faire en sorte que les permissions globales")
                .addLore("§adu territoire soient celles d'un autre territoire.")
                .addLore("")
                .addLore(
                        "§f§lActuellement : "
                                + (globalTrustLink == null ? "§c§lAucun" : "§a§l" + globalTrustLink.getName()))
                .build());
        inventory.setItem(13, new ItemBuilder(Head.HAL.get())
                .setDisplayName("§2Lien permissions joueurs")
                .addLore("§aPermet de faire en sorte que les permissions joueur")
                .addLore("§adu territoire soient celles d'un autre territoire.")
                .addLore("")
                .addLore(
                        "§f§lActuellement : "
                                + (trustLink == null ? "§c§lAucun" : "§a§l" + trustLink.getName()))
                .build());
        inventory.setItem(16, new ItemBuilder(Head.NO_ENTRY.get())
                .setDisplayName("§2Lien bannissements")
                .addLore("§aPermet de faire en sorte que les bannissements")
                .addLore("§adu territoire soient ceux d'un autre territoire.")
                .addLore("")
                .addLore(
                        "§f§lActuellement : "
                                + (banLink == null ? "§c§lAucun" : "§a§l" + banLink.getName()))
                .build());
        if (previousMenu != null) {
            inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§4Retour")
                    .addLore("§cRetourner au menu précédent")
                    .build());
        }
        fillWithGlass();
    }
}
