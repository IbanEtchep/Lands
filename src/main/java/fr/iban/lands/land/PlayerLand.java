package fr.iban.lands.land;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        if (player.getUniqueId().equals(owner)) {
            return true;
        }
        return super.isBypassing(player, action);
    }
}
