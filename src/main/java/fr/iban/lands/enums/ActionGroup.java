package fr.iban.lands.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ActionGroup {
    BUILDING("Construction/destruction", new ItemStack(Material.STONE)),
    CONTAINERS("Conteneurs", new ItemStack(Material.CHEST)),
    PLUGINS("Actions de plugins", new ItemStack(Material.BUCKET)),
    USE("Portes/redstone.", new ItemStack(Material.BIRCH_DOOR)),
    INTERACT("Intéractions", new ItemStack(Material.EMERALD)),
    TELEPORT("Téléportation", new ItemStack(Material.ENDER_PEARL));

    private final String displayName;
    private final ItemStack item;

    ActionGroup(String displayName, ItemStack item) {
        this.displayName = displayName;
        this.item = item;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getItem() {
        return item;
    }
}
