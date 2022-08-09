package fr.iban.lands.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Action {

    ALL("Tout", new ItemStack(Material.NETHER_STAR)),
    BLOCK_BREAK("Casser des blocs", new ItemStack(Material.IRON_PICKAXE)),
    BLOCK_PLACE("Placer des blocs", new ItemStack(Material.DIRT)),
    OPEN_CONTAINER("Ouvrir les conteneurs (coffre, fours, hoopers...)", new ItemStack(Material.CHEST)),
    BUCKET_EMPTY("Vider des sceaux", new ItemStack(Material.LAVA_BUCKET)),
    BUCKET_FILL("Remplir des sceaux", new ItemStack(Material.BUCKET)),
    USE("Utiliser les portes, trappes, repéteurs...", new ItemStack(Material.BIRCH_DOOR)),
    PASSIVE_KILL("Taper les entités passives", new ItemStack(Material.IRON_SWORD)),
    //AGGRESSIVE_KILL("Taper les entités aggressives", Material.DIAMOND_SWORD),
    SET_WARP("Mettre un warp dans votre claim", new ItemStack(Material.NETHER_STAR)),
    SET_HOME("Mettre une résidence (home) dans votre claim", new ItemStack(Material.BLUE_BED)),
    USE_BED("Utiliser les lits", new ItemStack(Material.RED_BED)),
    USE_ANVIL("Utiliser les enclumes", new ItemStack(Material.ANVIL)),
    TAGGED_KILL("Tuer les entités renomées", new ItemStack(Material.NAME_TAG)),
    SHOP_CREATE("Créer un shop dans votre warp", new ItemStack(Material.CHEST)),
    SHOP_OPEN("Ouvrir les shops dans votre warp", new ItemStack(Material.CHEST)),
    ENTITY_INTERACT("Interagir avec les entités (ex: villagois)", new ItemStack(Material.EMERALD)),
    LECTERN_TAKE("Prendre les livres sur les pupitres", new ItemStack(Material.LECTERN)),
    LECTERN_READ("Lire les livres dans les pupitres", new ItemStack(Material.LECTERN)),
    BREWING_STAND_INTERACT("Utiliser les alambics", new ItemStack(Material.BREWING_STAND)),
    PHYSICAL_INTERACT("Interactions physiques (plaques de pression, crochets...)", new ItemStack(Material.OAK_PRESSURE_PLATE)),
    OTHER_INTERACTS("Autres interactions (oeuf de dragon, pot de fleur, poudre d'os...)", new ItemStack(Material.DRAGON_EGG)),
    DROP("Jeter des items", new ItemStack(Material.ROTTEN_FLESH)),
    LEASH("Attacher un animal avec une laisse.", new ItemStack(Material.LEAD)),
    ARMOR_STAND_INTERACT("Interagir avec un porte armure.", new ItemStack(Material.ARMOR_STAND)),
    FROST_WALK("Générer de la glace avec l'enchantement semelles givrantes", new ItemStack(Material.PACKED_ICE)),
    CHORUS_TELEPORT("Se téléporter en mangeant des chorus", new ItemStack(Material.CHORUS_FRUIT)),
    ENDER_PEARL_TELEPORT("Se téléporter avec des ender pearls", new ItemStack(Material.ENDER_PEARL)),
    CAULDRON_FILL_EMPTY("Remplir/vider un chaudron", new ItemStack(Material.CAULDRON)),
    VEHICLE_PLACE_BREAK("Poser/casser des véhicules (bateaux, minecarts)", new ItemStack(Material.MINECART)),
    ;


    private String displayName;
    private ItemStack item;

    private Action(String displayName, ItemStack item) {
        this.displayName = displayName;
        this.item = item;
    }


    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getItem() {
        return item;
    }

    public static Action getByDisplayName(String displayName) {
        for (Action action : Action.values()) {
            if (displayName.contains(action.getDisplayName()))
                return action;
        }
        return null;
    }

}
