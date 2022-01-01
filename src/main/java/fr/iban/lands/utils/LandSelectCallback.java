package fr.iban.lands.utils;

import fr.iban.lands.objects.Land;

public interface LandSelectCallback {

	void select(Land land);

	void cancel();
}
