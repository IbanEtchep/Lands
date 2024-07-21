package fr.iban.lands.menus;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.api.LandService;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.events.PlayerLandFlagChangeEvent;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.utils.Head;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LandSettingsMenu extends PaginatedMenu {

    private final Land land;
    private final LandsPlugin plugin;
    private final LandService landService;
    private final LandRepository landRepository;
    private LandManageMenu previousMenu;
    private final List<Flag> flags;

    public LandSettingsMenu(Player player, Land land, LandsPlugin plugin) {
        super(player);
        this.land = land;
        this.plugin = plugin;
        this.landService = plugin.getLandService();
        this.landRepository = plugin.getLandRepository();

        flags = Arrays.stream(Flag.values())
                .filter(flag -> flag.isEnabled(land))
                .collect(Collectors.toList());
    }

    public LandSettingsMenu(Player player, Land land, LandsPlugin plugin, LandManageMenu previousMenu) {
        this(player, land, plugin);
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

        String displayName = item.getItemMeta().getDisplayName();
        Flag flag = Flag.getByDisplayName(displayName);

        if (flag != null) {
            if (displayName.startsWith("§4")) {
                PlayerLandFlagChangeEvent event = new PlayerLandFlagChangeEvent(player, land, flag, true);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    landRepository.addFlag(land, flag);
                }
            } else if (displayName.startsWith("§2")) {
                PlayerLandFlagChangeEvent event = new PlayerLandFlagChangeEvent(player, land, flag, false);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    landRepository.removeFlag(land, flag);
                }
            }
        }

        if (displayNameEquals(item, "§2Liens")) {
            new LinkManageMenu(player, plugin, land, this).open();
            return;
        }
        if (displayNameEquals(item, "§2Renommer")) {
            CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
            player.sendMessage("§2§lVeuillez entrer le nom du territoire souhaité (ou \"annuler\" pour annuler):");
            player.closeInventory();

            core.getTextInputs().put(player.getUniqueId(), texte -> {
                if (texte.equalsIgnoreCase("annuler")) {
                    open();
                } else {
                    landService.renameLand(land, player, texte);
                    open();
                    core.getTextInputs().remove(player.getUniqueId());
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
            inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§4Retour")
                    .addLore("§cRetourner au menu précédent")
                    .build());
        }
        inventory.setItem(27, new ItemBuilder(Head.OAK_L.get())
                .setName("§2Liens")
                .addLore("§aPermet de définir des liens avec vos autres territoires.")
                .build());
        inventory.setItem(35, new ItemBuilder(Material.NAME_TAG)
                .setName("§2Renommer")
                .addLore("§aPermet de renommer le territoire.")
                .build());
    }

    public Land getLand() {
        return land;
    }
}
