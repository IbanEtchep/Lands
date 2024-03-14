package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.ActionGroup;
import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Player;

public class GlobalTrustEditMenu extends TrustEditMenu {

    private final LandRepository landRepository;

    public GlobalTrustEditMenu(Player player, Land land, LandsPlugin plugin, Menu previousMenu, ActionGroup actionGroup) {
        super(player, land, plugin, previousMenu, actionGroup);
        this.landRepository = plugin.getLandRepository();
        this.trust = land.getGlobalTrust();
    }

    @Override
    public String getMenuName() {
        return "§2Permissions globales";
    }

    @Override
    protected void addTrust(Action action) {
        landRepository.addGlobalTrust(land, action);
        trust.addPermission(action);
    }

    @Override
    protected void removeTrust(Action action) {
        landRepository.removeGlobalTrust(land, action);
        trust.removePermission(action);
    }
}
