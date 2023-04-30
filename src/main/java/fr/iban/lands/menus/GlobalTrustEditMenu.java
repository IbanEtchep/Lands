package fr.iban.lands.menus;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.ActionGroup;
import fr.iban.lands.land.Land;
import org.bukkit.entity.Player;

public class GlobalTrustEditMenu extends TrustEditMenu{

	public GlobalTrustEditMenu(Player player, Land land, LandManager manager, Menu previousMenu, ActionGroup actionGroup) {
		super(player, land, manager, previousMenu, actionGroup);
		this.trust = land.getGlobalTrust();
	}

	@Override
	public String getMenuName() {
		return "§2Permissions globales";
	}

	@Override
	protected void addTrust(Action action) {
		manager.addGlobalTrust(land, action);
		trust.addPermission(action);
	}

	@Override
	protected void removeTrust(Action action) {
		manager.removeGlobalTrust(land, action);
		trust.removePermission(action);
	}
}
