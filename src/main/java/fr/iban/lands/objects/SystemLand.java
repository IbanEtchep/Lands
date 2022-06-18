package fr.iban.lands.objects;

import fr.iban.lands.enums.LandType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
