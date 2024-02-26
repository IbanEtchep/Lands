package fr.iban.lands.land;

import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.utils.Cuboid;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SubLand extends Land {

    private final Land superLand;
    private Cuboid cuboid;
    private String server;

    public SubLand(Land superLand, int id, String name) {
        super(id, name);
        this.superLand = superLand;
        setType(LandType.SUBLAND);
    }

    public void setCuboid(Cuboid cuboid, String server) {
        this.cuboid = cuboid;
        this.server = server;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public String getServer() {
        return server;
    }

    public Land getSuperLand() {
        return superLand;
    }

    @Override
    public boolean isBypassing(Player player, Action action) {
        UUID owner = getOwner();
        if (owner != null && player.getUniqueId().equals(owner)) {
            return true;
        }
        return super.isBypassing(player, action);
    }

    @Override
    public @Nullable UUID getOwner() {
        if (superLand instanceof PlayerLand pland) {
            return pland.getOwner();
        }
        return null;
    }
}
