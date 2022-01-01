package fr.iban.lands.objects;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import fr.iban.lands.enums.LandType;

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
