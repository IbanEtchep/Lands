package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.ActionGroup;
import fr.iban.lands.model.Trust;
import fr.iban.lands.model.land.Land;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class TrustEditMenu extends PaginatedMenu {

    protected Land land;
    protected LandsPlugin plugin;
    protected Menu previousMenu;
    protected Trust trust;
    protected ActionGroup actionGroup;
    protected Map<Integer, Consumer<InventoryClickEvent>> clickActionAtSlot = new HashMap<>();

    public TrustEditMenu(Player player, Land land, LandsPlugin plugin, Menu previousMenu, ActionGroup actionGroup) {
        super(player);
        this.land = land;
        this.plugin = plugin;
        this.previousMenu = previousMenu;
        this.actionGroup = actionGroup;
    }

    @Override
    public int getRows() {
        return 4;
    }

    @Override
    public int getElementAmount() {
        if (actionGroup == null) {
            return Action.values().length + ActionGroup.values().length + 1;
        } else {
            return Action.getActionsGrouped(actionGroup).size();
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        checkBottonsClick(item, player);
        if (previousMenu != null && displayNameEquals(item, "§4Retour")) {
            if (this.actionGroup == null) {
                previousMenu.open();
            } else {
                this.actionGroup = null;
                open();
            }
            return;
        }

        if (clickActionAtSlot.containsKey(e.getSlot())) {
            clickActionAtSlot.get(e.getSlot()).accept(e);
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        if (previousMenu != null) {
            inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§4Retour")
                    .addLore("§cRetourner au menu précédent")
                    .build());
        }

        int startIndex = getMaxItemsPerPage() * page;
        int currentIndex = 0;

        if (actionGroup == null) {
            if (page == 0) {
                addActionToInventory(Action.ALL);
                if (trust.hasPermission(Action.ALL)) {
                    return;
                }
                currentIndex++;
            }

            for (ActionGroup group : ActionGroup.values()) {
                if (shouldAddItem(currentIndex, startIndex)) {
                    addActionGroupToInventory(group);
                }
                currentIndex++;
            }
        }

        List<Action> actions = Action.getActionsGrouped(actionGroup);

        for (Action action : actions) {
            if (action == Action.ALL) continue;
            if (shouldAddItem(currentIndex, startIndex)) {
                addActionToInventory(action);
            }
            currentIndex++;
        }
    }

    private boolean shouldAddItem(int currentIndex, int startIndex) {
        return currentIndex >= startIndex && currentIndex < startIndex + getMaxItemsPerPage();
    }

    protected abstract void addTrust(Action action);

    protected abstract void removeTrust(Action action);

    private void addActionGroupToInventory(ActionGroup actionGroup) {
        int slot = inventory.firstEmpty();

        clickActionAtSlot.put(slot, clickEvent -> {
            this.actionGroup = actionGroup;
            open();
        });

        ItemBuilder itemBuilder = new ItemBuilder(actionGroup.getItem().clone())
                .setName("§f§l" + actionGroup.getDisplayName());

        for (Action action : Action.getActionsGrouped(actionGroup)) {
            if (trust.hasPermission(action)) {
                itemBuilder.addLore("§f• §2" + action.getDisplayName());
            } else {
                itemBuilder.addLore("§f• §4" + action.getDisplayName());
            }
        }

        inventory.setItem(slot, itemBuilder.build());
    }

    private void addActionToInventory(Action action) {
        int slot = inventory.firstEmpty();

        clickActionAtSlot.put(slot, event -> {
            if (trust.hasPermission(action)) {
                removeTrust(action);
            } else {
                addTrust(action);
            }
            super.open();
        });

        if (trust.hasPermission(action)) {
            inventory.setItem(slot, new ItemBuilder(action.getItem().clone())
                    .setName("§2" + action.getDisplayName())
                    .build());
        } else {
            inventory.setItem(slot, new ItemBuilder(action.getItem().clone())
                    .setName("§4" + action.getDisplayName())
                    .build());
        }
    }
}
