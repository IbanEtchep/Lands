package fr.iban.lands.utils;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.entity.EntityType;

public class MobUtils {

	public static transient Set<EntityType> mobsList = EnumSet.of(
			EntityType.ZOMBIE, EntityType.ZOGLIN, EntityType.ENDER_DRAGON,
			EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.SPIDER,
			EntityType.SKELETON, EntityType.ZOMBIFIED_PIGLIN, EntityType.CREEPER,
			EntityType.DROWNED, EntityType.PILLAGER, EntityType.WITCH,
			EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.SLIME,
			EntityType.ZOMBIE_VILLAGER, EntityType.ENDERMAN, EntityType.EVOKER,
			EntityType.RAVAGER, EntityType.VEX, EntityType.VINDICATOR,
			EntityType.PHANTOM, EntityType.MAGMA_CUBE, EntityType.ENDERMITE, EntityType.STRAY);
	
}
