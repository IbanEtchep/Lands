package fr.iban.lands.menus;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.model.Trust;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.utils.ChatUtils;
import fr.iban.lands.utils.Head;
import fr.iban.lands.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TrustsManageMenu extends PaginatedMenu {

    private final Land land;
    private final Map<UUID, Trust> trusts;
    private final LandsPlugin plugin;
    private final Map<Integer, UUID> uuidAtSlot = new HashMap<>();
    private LandManageMenu previousMenu;

    public TrustsManageMenu(Player player, LandsPlugin plugin, Land land) {
        super(player);
        this.plugin = plugin;
        this.land = land;
        this.trusts = land.getTrusts();
    }

    public TrustsManageMenu(Player player, LandsPlugin plugin, Land land, LandManageMenu previousMenu) {
        this(player, plugin, land);
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

        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        if (previousMenu != null && displayNameEquals(item, "§4Retour")) {
            previousMenu.open();
        }

        if (item != null && item.getType() == Material.PLAYER_HEAD) {
            if (displayNameEquals(item, "§2Ajouter")) {
                player.closeInventory();
                player.sendMessage(
                        "§2§lVeuillez entrer le nom du joueur à qui vous voulez modifier les permissions, ou tapez annuler :");
                CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
                core.getTextInputs().put(player.getUniqueId(), component -> {
                    String text = ChatUtils.toPlainText(component);

                    if (text.equalsIgnoreCase("annuler")) {
                        core.getTextInputs().remove(player.getUniqueId());
                        open();
                        return;
                    }

                    Player target = Bukkit.getPlayer(text);

                    if (target == null) {
                        player.sendMessage("§cCe joueur n'est pas en ligne.");
                        open();
                    } else {
                        new PlayerTrustEditMenu(player, target.getUniqueId(), land, plugin, this, null).open();
                    }

                    core.getTextInputs().remove(player.getUniqueId());
                });
                return;
            }

            if (displayNameEquals(item, "§2Permissions globales")) {
                new GlobalTrustEditMenu(player, land, plugin, this, null).open();
                return;
            }

            if (displayNameEquals(item, "§2Permissions de guilde")) {
                new GuildTrustEditMenu(player, land, plugin, this).open();
            }

            UUID uuid = uuidAtSlot.get(e.getSlot());

            if (uuid == null) {
                return;
            }

            new PlayerTrustEditMenu(player, uuid, land, plugin, this, null).open();
        }
    }

    @Override
    public void setMenuItems() {
        uuidAtSlot.clear();
        addMenuBorder();

        if (trusts != null && !trusts.isEmpty()) {
            List<UUID> uuids = new ArrayList<>(trusts.keySet());
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if (index >= uuids.size()) break;
                if (uuids.get(index) != null) {
                    final int slot = inventory.firstEmpty();
                    UUID warp = uuids.get(index);
                    uuidAtSlot.put(slot, warp);
                    inventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("§cChargement...").build());
                    getTrustItem(uuids.get(index)).thenAccept(item -> inventory.setItem(slot, item));
                }
            }
        }
        inventory.setItem(27, new ItemBuilder(Head.GLOBE.get())
                .setName("§2Permissions globales")
                .addLore("§aPermet de définir des permissions")
                .addLore("§aqui seront appliquées à tout le monde.")
                .build());

        AbstractGuildDataAccess guildDataAccess = plugin.getGuildDataAccess();
        if (plugin.isGuildsHookEnabled() && land.getOwner() != null && guildDataAccess.hasGuild(player.getUniqueId())) {
            inventory.setItem(28, new ItemBuilder(Head.HOUSE_ORANGE.get())
                    .setName("§2Permissions de guilde")
                    .addLore("§aPermet de définir les permissions")
                    .addLore("§ades membres de votre guilde.")
                    .build());
        }

        inventory.setItem(35, new ItemBuilder(Head.OAK_PLUS.get())
                .setName("§2Ajouter")
                .addLore("§aPermet d'ajouter un joueur pour éditer ses permissions.")
                .build());

        if (previousMenu != null) {
            inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§4Retour")
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
            return new ItemBuilder(head)
                    .setDisplayName("§2§l" + offlinePlayer.getName())
                    .addLore("§aClic gauche pour éditer")
                    .addLore("§cClic droit pour supprimer")
                    .build();
        });
    }
}
