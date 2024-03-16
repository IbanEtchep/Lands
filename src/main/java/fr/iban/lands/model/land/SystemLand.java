package fr.iban.lands.model.land;

import fr.iban.lands.enums.LandType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SystemLand extends Land {

    public SystemLand(UUID id, String name) {
        super(id, name);
    }

    @Override
    public @Nullable UUID getOwner() {
        return null;
    }

    @Override
    public LandType getType() {
        return LandType.SYSTEM;
    }
}
