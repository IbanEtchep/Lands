package fr.iban.lands.listeners;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.Util;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.GuildLand;
import fr.iban.lands.model.land.Land;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShopListeners implements Listener {

    private final LandsPlugin plugin;
    private final LandRepository landRepository;
    List<UUID> tempStaffs = new ArrayList<>();

    public ShopListeners(LandsPlugin landsPlugin) {
        this.plugin = landsPlugin;
        this.landRepository = landsPlugin.getLandRepository();
    }

    @EventHandler
    public void onShopCreate(ShopPreCreateEvent e) {
        Land land = landRepository.getLandAt(e.getLocation());
        Optional<Player> bukkitPlayer = e.getCreator().getBukkitPlayer();

        if (bukkitPlayer.isEmpty()) {
            return;
        }

        if (!land.isBypassing(bukkitPlayer.get(), Action.SHOP_CREATE)) {
            e.setCancelled(true, "Vous ne pouvez pas créer de shop ici.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void beforeCheck(PlayerInteractEvent event) {
        final Block b = event.getClickedBlock();

        if (b == null) {
            return;
        }

        if (!Util.canBeShop(b)) {
            return;
        }

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return; // Didn't right click it, we dont care.
        }

        final Shop shop = getShopPlayer(b.getLocation(), true);
        // Make sure they're not using the non-shop half of a double chest.
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Land land = landRepository.getLandAt(b.getLocation());
        if (!land.isBypassing(player, Action.SHOP_OPEN)) {
            return;
        }

        BuiltInShopPermissionGroup staffGroup = BuiltInShopPermissionGroup.STAFF;

        if (shop.playersCanAuthorize(staffGroup).contains(uuid)) {
            shop.setPlayerGroup(uuid, staffGroup);
            tempStaffs.add(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void afterCheck(PlayerInteractEvent event) {
        final Block b = event.getClickedBlock();

        if (b == null) {
            return;
        }

        if (!Util.canBeShop(b)) {
            return;
        }

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return; // Didn't right click it, we dont care.
        }

        final Shop shop = getShopPlayer(b.getLocation(), true);
        // Make sure they're not using the non-shop half of a double chest.
        if (shop == null) {
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();

        if (tempStaffs.contains(uuid)) {
            shop.setPlayerGroup(uuid, BuiltInShopPermissionGroup.EVERYONE);
            tempStaffs.remove(uuid);
        }
    }

    @Nullable
    public Shop getShopPlayer(@NotNull Location location, boolean includeAttached) {
        return includeAttached
                ? QuickShopAPI.getInstance().getShopManager().getShopIncludeAttached(location)
                : QuickShopAPI.getInstance().getShopManager().getShop(location);
    }

    @EventHandler
    public void onPurchase(ShopSuccessPurchaseEvent e) {
        Shop shop = e.getShop();

        if (shop.getShopType() == ShopType.BUYING) return;

        if (plugin.getEconomy() == null) return;

        if (!plugin.isGuildsHookEnabled()) return;

        if (landRepository.getLandAt(e.getShop().getLocation()) instanceof GuildLand guildLand) {
            UUID seller = shop.getOwner().getUniqueId();

            if (!guildLand.hasFlag(Flag.SHOP_MONEY_TO_GUILD_BANK) || seller == null) return;
            
            double price = e.getBalance();
            boolean success = plugin.getGuildDataAccess().deposit(guildLand.getGuildId(), price);
            if (success) {
                EconomyResponse response = plugin.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(seller), price);
                if (!response.transactionSuccess()) {
                    plugin.getGuildDataAccess().withdraw(guildLand.getGuildId(), price);
                }
            }
        }
    }
}
