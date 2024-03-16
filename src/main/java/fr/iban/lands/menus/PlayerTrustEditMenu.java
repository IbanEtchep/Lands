package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.ActionGroup;
import fr.iban.lands.model.land.Land;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerTrustEditMenu extends TrustEditMenu {

    private final UUID target;
    private final LandRepository landRepository;

    public PlayerTrustEditMenu(Player player, UUID target, Land land, LandsPlugin plugin, Menu previousMenu, ActionGroup actionGroup) {
        super(player, land, plugin, previousMenu, actionGroup);
        this.landRepository = plugin.getLandRepository();
        this.target = target;
        this.trust = land.getTrust(target);
    }

    @Override
    public String getMenuName() {
        return "§2Permissions de " + Bukkit.getOfflinePlayer(target).getName();
    }

    protected void addTrust(Action action) {
        landRepository.addTrust(land, target, action);
        trust.getPermissions().add(action);
    }

    protected void removeTrust(Action action) {
        landRepository.removeTrust(land, target, action);
        trust.getPermissions().remove(action);
    }
}
