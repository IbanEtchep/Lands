package fr.iban.lands.menus;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.ConfirmMenu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.land.Land;
import fr.iban.lands.utils.DateUtils;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LandMainMenu extends PaginatedMenu {

    private final LandManager manager;
    private final List<Land> lands;
    private final LandsPlugin plugin;
    private final LandType landType;

    private final UUID landOwner;

    public LandMainMenu(Player player, LandsPlugin plugin, List<Land> lands, LandType landType, UUID landOwner) {
        super(player);
        this.manager = plugin.getLandManager();
        this.lands = lands;
        this.plugin = plugin;
        this.landType = landType;
        this.landOwner = landOwner;
    }

    @Override
    public String getMenuName() {
        return switch (landType) {
            case SYSTEM -> "§2§lTerritoires système:";
            case SUBLAND -> "§2§lSous-territoires :";
            case GUILD -> "§2§lTerritoires guilde :";
            default -> "§2§lVos territoires :";
        };
    }

    @Override
    public int getRows() {
        return 4;
    }

    @Override
    public int getElementAmount() {
        return lands.size();
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();

        if (current == null) return;

        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        checkBottonsClick(current, player);

        if (displayNameEquals(current, "§2Créer")) {
            CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
            player.closeInventory();
            player.sendMessage("§2§lVeuillez entrer le nom du territoire souhaité :");
            core.getTextInputs()
                    .put(
                            player.getUniqueId(),
                            texte -> {
                                switch (landType) {
                                    case PLAYER:
                                        manager.createLand(player, texte)
                                                .thenAccept(land -> Bukkit.getScheduler().runTask(plugin, () -> {
                                                    lands.add(land);
                                                    open();
                                                }));
                                        break;
                                    case SYSTEM:
                                        manager.createSystemLand(player, texte)
                                                .thenAccept(land -> Bukkit.getScheduler().runTask(plugin, () -> {
                                                    lands.add(land);
                                                    open();
                                                }));
                                        break;
                                    case GUILD:
                                        manager
                                                .createGuildLand(player, texte)
                                                .thenAccept(land -> Bukkit.getScheduler().runTask(plugin, () -> {
                                                    lands.add(land);
                                                    open();
                                                }));
                                        break;
                                    default:
                                        break;
                                }
                                core.getTextInputs().remove(player.getUniqueId());
                            });
        }

        if (current.getType() == Material.PLAYER_HEAD) {
            Land land = getLandByName(current.getItemMeta().getDisplayName());

            if (land == null) {
                return;
            }

            ClickType click = e.getClick();
            if (click == ClickType.RIGHT) {
                new ConfirmMenu(
                        player,
                        "§cConfirmation de suppression",
                        "§eVoulez-vous supprimer le territoire " + land.getName() + " ?",
                        result -> {
                            if (result) {
                                lands.remove(land);
                                manager
                                        .deleteLand(land)
                                        .thenRun(
                                                () -> {
                                                    player.sendMessage(
                                                            "§cLe territoire " + land.getName() + " a bien été supprimé.");
                                                });
                            }
                            super.open();
                        })
                        .open();
            } else if (click == ClickType.LEFT) {
                new LandManageMenu(player, plugin, manager, land, this).open();
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        if (lands != null && !lands.isEmpty()) {
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if (index >= lands.size()) break;
                if (lands.get(index) != null) {
                    Land land = lands.get(index);
                    inventory.addItem(getLandItem(land));
                }
            }
        }

        if (landOwner != null) {
            inventory.setItem(27,
                    new ItemBuilder(Head.OAK_INFO.get())
                            .setName("§2Informations")
                            .addLore("§cChargement...")
                            .build());
            getInfoItem().thenAccept(item -> inventory.setItem(27, item));
        }

        inventory.setItem(
                35,
                new ItemBuilder(Head.OAK_PLUS.get())
                        .setName("§2Créer")
                        .addLore("§aPermet de créer un nouveau territoire")
                        .build());
    }

    private ItemStack getLandItem(Land land) {
        ItemBuilder itemBuilder =
                new ItemBuilder(Head.GRASS.get())
                        .setDisplayName("§2" + land.getName())
                        .addLore("§aClic gauche pour gérer ce territoire.")
                        .addLore("§cClic droit pour supprimer ce territoire.")
                        .addLore("")
                        .addLore("§fTronçons : " + manager.getChunks(land).size())
                        .addLore("§fJoueurs trust : " + land.getTrusts().size())
                        .addLore("§fBannissements : " + land.getBans().size());
        if (land.getTotalWeeklyPrice() > 0) {
            itemBuilder
                    .addLore(
                            "§fCoût hebdomadaire : " + plugin.getEconomy().format(land.getTotalWeeklyPrice()))
                    .addLore(
                            land.getNextPaiement() != null
                                    ? "§fProchain paiement : " + DateUtils.format(land.getNextPaiement())
                                    : "");
        }
        if (land.isPaymentDue()) {
            itemBuilder
                    .addLore("")
                    .addLore("§4§L⚠ Défaut de paiement.")
                    .addLore(
                            "§fVous devez payer " + plugin.getEconomy().format(land.getTotalWeeklyPrice()) + ". ")
                    .addLore("§fUtilisez la commande §b/land pay " + land.getName() + ".");
        }
        return itemBuilder.build();
    }

    private Land getLandByName(String string) {
        for (Land land : lands) {
            if (ChatColor.stripColor(string).equals(land.getName())) {
                return land;
            }
        }
        return null;
    }

    private CompletableFuture<ItemStack> getInfoItem() {
        return manager.future(
                () -> {
                    int count = manager.getChunkCount(landOwner).join();
                    int maxCount = manager.getMaxChunkCount(landOwner).join();

                    ItemBuilder itemBuilder = new ItemBuilder(Head.OAK_INFO.get())
                            .setName("§2Informations")
                            .addLore("§f§lTronçons utilisés : §a" + count + "/" + maxCount);

                    if (landType == LandType.GUILD) {
                        int personalCount = manager.getChunkCount(player.getUniqueId()).join();
                        int personalMaxCount = manager.getMaxChunkCount(player.getUniqueId()).join();
                        itemBuilder.addLore("§f§lTronçons personnels : §a" + personalCount + "/" + personalMaxCount);

                        itemBuilder.addLore("§7Si la guilde n'a plus de tronçons, vos ");
                        itemBuilder.addLore("§7tronçons personnels seront utilisés.");
                    }

                    return itemBuilder.build();
                }
        );
    }
}
