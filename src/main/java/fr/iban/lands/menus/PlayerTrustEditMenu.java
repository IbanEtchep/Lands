package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.ActionGroup;
import fr.iban.lands.land.Land;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerTrustEditMenu extends TrustEditMenu {

    private final UUID target;

    public PlayerTrustEditMenu(
            Player player,
            UUID target,
            Land land,
            LandManager manager,
            Menu previousMenu,
            ActionGroup actionGroup) {
        super(player, land, manager, previousMenu, actionGroup);
        this.target = target;
        this.trust = land.getTrust(target);
    }

    @Override
    public String getMenuName() {
        return "§2Permissions de " + Bukkit.getOfflinePlayer(target).getName();
    }

    protected void addTrust(Action action) {
        manager.addTrust(land, target, action);
        trust.getPermissions().add(action);
    }

    protected void removeTrust(Action action) {
        manager.removeTrust(land, target, action);
        trust.getPermissions().remove(action);
    }
}
