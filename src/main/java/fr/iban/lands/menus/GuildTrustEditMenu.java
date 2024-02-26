package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.land.Land;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuildTrustEditMenu extends PaginatedMenu {

    private final Land land;
    private final LandManager manager;
    private TrustsManageMenu previousMenu;

    public GuildTrustEditMenu(Player player, Land land, LandManager manager) {
        super(player);
        this.land = land;
        this.manager = manager;
    }

    public GuildTrustEditMenu(
            Player player, Land land, LandManager manager, TrustsManageMenu previousMenu) {
        this(player, land, manager);
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "§2Permissions de la guilde";
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

        if (item != null && previousMenu != null && displayNameEquals(item, "§4Retour")) {
            previousMenu.open();
            return;
        }

        if (item != null) {
            if (item.getItemMeta().getDisplayName().startsWith("§4")) {
                manager.addGuildTrust(land, Action.getByDisplayName(item.getItemMeta().getDisplayName()));
            } else {
                manager.removeGuildTrust(
                        land, Action.getByDisplayName(item.getItemMeta().getDisplayName()));
            }
        }
        super.open();
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        //		for(Action action : Action.values()) {
        //			Bukkit.broadcastMessage(action.toString());
        //		}
        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;
            if (index >= Action.values().length) break;
            Action action = Action.values()[index];
            if (action != null) {
                if (land.getGuildTrust().hasPermission(action)) {
                    inventory.addItem(
                            new ItemBuilder(action.getItem()).setName("§2" + action.getDisplayName()).build());
                } else {
                    inventory.addItem(
                            new ItemBuilder(action.getItem()).setName("§4" + action.getDisplayName()).build());
                }
            }
        }

        if (previousMenu != null) {
            inventory.setItem(
                    31,
                    new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                            .setDisplayName("§4Retour")
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
