package fr.iban.lands.objects;

import java.util.HashSet;
import java.util.Set;

import fr.iban.lands.enums.Action;

public class Trust {
	
	private Set<Action> permissions = new HashSet<>();
	

	public Set<Action> getPermissions() {
		return permissions;
	}
	
	public void addPermission(Action action) {
		permissions.add(action);
	}
	
	public void removePermission(Action action) {
		permissions.remove(action);
	}
	
	public boolean hasPermission(Action action) {
		return permissions.contains(action) || permissions.contains(Action.ALL);
	}

}
