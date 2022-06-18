package fr.iban.lands.objects;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerLand extends Land {

	private UUID owner;

	public PlayerLand(int id, UUID owner, String name) {
		super(id, name);
		this.owner = owner;
		setType(LandType.PLAYER);
	}
	
	@Override
	public UUID getOwner() {
		return owner;
	}
	
	@Override
	public boolean isBypassing(Player player, Action action) {
		if(player.getUniqueId().equals(owner)) {
			return true;
		}
		return super.isBypassing(player, action);
	}

}
