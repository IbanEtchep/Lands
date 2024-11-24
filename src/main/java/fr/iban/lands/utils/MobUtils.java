package fr.iban.lands.utils;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.entity.EntityType;

public class MobUtils {

    public static Set<EntityType> blockEntityList =
            EnumSet.of(EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, EntityType.ARMOR_STAND);
}
