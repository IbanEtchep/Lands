package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuildTrustEditMenu extends PaginatedMenu {

    private final Land land;
    private final LandRepository landRepository;
    private TrustsManageMenu previousMenu;

    public GuildTrustEditMenu(Player player, Land land, LandsPlugin plugin) {
        super(player);
        this.land = land;
        this.landRepository = plugin.getLandRepository();
    }

    public GuildTrustEditMenu(
            Player player, Land land, LandsPlugin plugin, TrustsManageMenu previousMenu) {
        this(player, land, plugin);
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
            String displayName = item.getItemMeta().getDisplayName();
            Action action = Action.getByDisplayName(displayName);

            if (displayName.startsWith("§4")) {
                landRepository.addGuildTrust(land, action);
            } else {
                landRepository.removeGuildTrust(land, action);
            }
        }
        super.open();
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;

            if (index >= Action.values().length) break;

            Action action = Action.values()[index];

            if (action != null) {
                ItemBuilder item = new ItemBuilder(action.getItem());

                if (land.getGuildTrust().hasPermission(action)) {
                    item.setName("§2" + action.getDisplayName());
                } else {
                    item.setName("§4" + action.getDisplayName());
                }

                inventory.addItem(item.build());
            }
        }

        if (previousMenu != null) {
            inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§4Retour")
                    .addLore("§cRetourner au menu précédent")
                    .build());
        }
    }
}
