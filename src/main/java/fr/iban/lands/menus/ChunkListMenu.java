package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.Land;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChunkListMenu extends PaginatedMenu {

    private final LandsPlugin plugin;
    private final Land land;
    private final List<SChunk> chunks = new ArrayList<>();
    private Map<Integer, SChunk> chunkAtSlot;
    private Menu previousMenu;

    public ChunkListMenu(Player player, LandsPlugin plugin, Land land) {
        super(player);
        this.plugin = plugin;
        this.land = land;
        chunks.addAll(plugin.getLandRepository().getChunks(land));
    }

    public ChunkListMenu(Player player, LandsPlugin plugin, Land land, Menu previousMenu) {
        this(player, plugin, land);
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "§2§l" + land.getName() + " §8> §2tronçons";
    }

    @Override
    public int getRows() {
        return 4;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (item == null) return;

        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        if (previousMenu != null && displayNameEquals(item, "§4Retour")) {
            previousMenu.open();
        }

        checkBottonsClick(item, player);

        SChunk schunk = chunkAtSlot.get(e.getSlot());

        if (schunk == null) {
            return;
        }

        plugin.getLandService().unclaim(schunk);
        chunks.remove(schunk);
        open();
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        chunkAtSlot = new HashMap<>();

        if (!chunks.isEmpty()) {
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;

                if (index >= chunks.size()) break;

                if (chunks.get(index) != null) {
                    final int slot = inventory.firstEmpty();
                    SChunk schunk = chunks.get(index);
                    inventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("§cChargement...").build());
                    chunkAtSlot.put(slot, schunk);
                    getChunkItem(schunk).thenAccept(item -> inventory.setItem(slot, item));
                }
            }
        }

        if (previousMenu != null) {
            inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§4Retour")
                    .addLore("§cRetourner au menu précédent")
                    .build());
        }
    }

    @Override
    public int getElementAmount() {
        return chunks.size();
    }

    private CompletableFuture<ItemStack> getChunkItem(SChunk chunk) {
        return CompletableFuture.supplyAsync(() -> new ItemBuilder(Material.DIRT)
                .setDisplayName("§2§lTronçon")
                .addLore("§fServeur : §a§l" + chunk.getServer())
                .addLore("§fMonde : §a§l" + chunk.getWorld())
                .addLore("§fCoordonnées (en chunk) : X: §a§l" + chunk.getX() + " §fZ: §a§l" + chunk.getZ())
                .addLore("§fCoordonées (en bloc) : X: §a§l" + (chunk.getX() * 16) + " §fZ: §a§l" + (chunk.getZ() * 16))
                .addLore("§cCliquez pour supprimer.")
                .build());
    }
}
