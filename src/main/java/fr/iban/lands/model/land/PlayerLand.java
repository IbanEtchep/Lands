package fr.iban.lands.model.land;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLand extends Land {

    private final UUID owner;

    public PlayerLand(UUID id, UUID owner, String name) {
        super(id, name);
        this.owner = owner;
    }

    @NotNull
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public LandType getType() {
        return LandType.PLAYER;
    }

    @Override
    public boolean isBypassing(Player player, Action action) {
        if (player.getUniqueId().equals(owner)) {
            return true;
        }
        return super.isBypassing(player, action);
    }
}
