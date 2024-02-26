package fr.iban.lands.land;

import fr.iban.lands.enums.LandType;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class SystemLand extends Land {

    public SystemLand(int id, String name) {
        super(id, name);
        setType(LandType.SYSTEM);
    }

    @Override
    public @Nullable UUID getOwner() {
        return null;
    }
}
