package fr.iban.lands.menus;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.events.LandFlagChangeEvent;
import fr.iban.lands.land.Land;
import fr.iban.lands.utils.Head;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LandSettingsMenu extends PaginatedMenu {

    private final Land land;
    private final LandManager manager;
    private LandManageMenu previousMenu;
    private final List<Flag> flags;

    public LandSettingsMenu(Player player, Land land, LandManager manager) {
        super(player);
        this.land = land;
        this.manager = manager;
        flags =
                Arrays.stream(Flag.values())
                        .filter(flag -> flag.isEnabled(land))
                        .collect(Collectors.toList());
    }

    public LandSettingsMenu(
            Player player, Land land, LandManager manager, LandManageMenu previousMenu) {
        this(player, land, manager);
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "§2§l" + land.getName() + " §8> §2paramètres";
    }

    @Override
    public int getRows() {
        return 4;
    }

    @Override
    public int getElementAmount() {
        return flags.size();
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        checkBottonsClick(item, player);
        if (previousMenu != null && displayNameEquals(item, "§4Retour")) {
            previousMenu.open();
            return;
        }

        if (item.getItemMeta().getDisplayName().startsWith("§4")) {
            Flag flag = Flag.getByDisplayName(item.getItemMeta().getDisplayName());
            LandFlagChangeEvent event = new LandFlagChangeEvent(player, land, flag, true);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                manager.addFlag(land, flag);
            }
        } else {
            Flag flag = Flag.getByDisplayName(item.getItemMeta().getDisplayName());
            LandFlagChangeEvent event = new LandFlagChangeEvent(player, land, flag, false);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                manager.removeFlag(land, flag);
            }
        }

        if (displayNameEquals(item, "§2Liens")) {
            new LinkManageMenu(player, manager, land, this).open();
            return;
        }
        if (displayNameEquals(item, "§2Renommer")) {
            CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
            player.sendMessage(
                    "§2§lVeuillez entrer le nom du territoire souhaité (ou \"annuler\" pour annuler):");
            player.closeInventory();
            core.getTextInputs()
                    .put(
                            player.getUniqueId(),
                            texte -> {
                                if (texte.equalsIgnoreCase("annuler")) {
                                    open();
                                } else {
                                    manager
                                            .renameLand(land, player, texte)
                                            .thenRun(
                                                    () ->
                                                            Bukkit.getScheduler()
                                                                    .runTask(
                                                                            LandsPlugin.getInstance(),
                                                                            () -> {
                                                                                open();
                                                                                core.getTextInputs().remove(player.getUniqueId());
                                                                            }));
                                }
                            });
            return;
        }

        super.open();
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;
            if (index >= getElementAmount()) break;
            Flag flag = flags.get(index);
            if (flag != null) {
                if (land.hasFlag(flag)) {
                    inventory.addItem(
                            new ItemBuilder(flag.getItem()).setName("§2" + flag.getDisplayName()).build());
                } else {
                    inventory.addItem(
                            new ItemBuilder(flag.getItem()).setName("§4" + flag.getDisplayName()).build());
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
        inventory.setItem(
                27,
                new ItemBuilder(Head.OAK_L.get())
                        .setName("§2Liens")
                        .addLore("§aPermet de définir des liens avec vos autres territoires.")
                        .build());
        inventory.setItem(
                35,
                new ItemBuilder(Material.NAME_TAG)
                        .setName("§2Renommer")
                        .addLore("§aPermet de renommer le territoire.")
                        .build());
    }

    public LandManager getManager() {
        return manager;
    }

    public Land getLand() {
        return land;
    }
}
