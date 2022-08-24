package fr.iban.lands.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Flag {
	INVINCIBLE("Active l'invincibilité pour les joueurs", new ItemStack(Material.IRON_SWORD), LandType.SYSTEM),
	PVP("Active le pvp", new ItemStack(Material.DIAMOND_SWORD), LandType.SYSTEM),
	AUTO_REPLANT("Active l'auto replantation", new ItemStack(Material.GOLDEN_SHOVEL), LandType.SYSTEM),
	DOORS_AUTOCLOSE("Active la fermeture automatique des portes", new ItemStack(Material.OAK_DOOR), LandType.SYSTEM),
	NO_MOB_SPAWNING("Désactive le spawn des mobs", new ItemStack(Material.ZOMBIE_SPAWN_EGG), LandType.SYSTEM),
	INVISIBLE("Rend les joueurs à l'intérieur invisibles.", new ItemStack(Material.POTION), LandType.SYSTEM),
	EXPLOSIONS("Active les explosions", new ItemStack(Material.TNT)),
	BLOCK_DAMAGES_BY_ENTITY("Active les dégâts aux blocs par les entités (endermans, withers...)", new ItemStack(Material.DIRT)),
	PRESSURE_PLATE_BY_ENTITY("Active l'utilisation des plaques de pression par les entités", new ItemStack(Material.OAK_PRESSURE_PLATE)),
	TRIPWIRE_BY_ENTITY("Active l'utilisation des crochets par les entités", new ItemStack(Material.TRIPWIRE_HOOK)),
	FARMLAND_GRIEF("Active la destruction des terres labourées", new ItemStack(Material.FARMLAND)),
	FIRE("Active les dégâts du feu et sa propagation.", new ItemStack(Material.FLINT_AND_STEEL)),
	SILENT_MOBS("Désactive le bruit des mobs de ce territoire", new ItemStack(Material.JUKEBOX)),
	LIQUID_SPREAD("Autorise les liquides extérieurs au claim à se propager.", new ItemStack(Material.LAVA_BUCKET)),
	SHOP_MONEY_TO_GUILD_BANK("Rediriger l'argent des shops à la banque de la guilde.", new ItemStack(Material.GOLD_INGOT), LandType.GUILD)
	;

	private final String displayName;
	private final ItemStack item;
	private final LandType[] enabledLandTypes;

	Flag(String displayName, ItemStack item, LandType... enabledTypes) {
		this.displayName = displayName;
		this.item = item;
		this.enabledLandTypes = enabledTypes;
	}

	Flag(String displayName, ItemStack item) {
		this.displayName = displayName;
		this.item = item;
		this.enabledLandTypes = LandType.values();
	}



	public String getDisplayName() {
		return displayName;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public static Flag getByDisplayName(String displayName) {
		for (Flag action : values()) {
			if(displayName.contains(action.getDisplayName()))
				return action;
		}
		return null;
	}


	public boolean isEnabled(LandType landType) {
		for (LandType value : enabledLandTypes) {
			if(value == landType) {
				return true;
			}
		}
		return false;
	}
}
