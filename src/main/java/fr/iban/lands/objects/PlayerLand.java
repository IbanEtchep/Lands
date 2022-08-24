package fr.iban.lands.objects;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLand extends Land {

	private final UUID owner;

	public PlayerLand(int id, UUID owner, String name) {
		super(id, name);
		this.owner = owner;
		setType(LandType.PLAYER);
	}

	@NotNull
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
