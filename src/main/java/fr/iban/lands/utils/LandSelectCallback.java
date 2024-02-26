package fr.iban.lands.utils;

import fr.iban.lands.land.Land;

public interface LandSelectCallback {

    void select(Land land);

    void cancel();
}
