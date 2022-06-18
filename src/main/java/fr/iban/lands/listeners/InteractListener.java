package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.objects.Land;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractListener implements Listener {

	private LandManager landmanager;
	private LandsPlugin plugin;

	public InteractListener(LandsPlugin landsPlugin) {
		this.plugin = landsPlugin;
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler
	public void onPhysics(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();
		if(block == null)
			return;

		Land land = landmanager.getLandAt(block.getLocation());

		if(land == null)
			return;

		if(e.getAction() == org.bukkit.event.block.Action.PHYSICAL && block.getType() == Material.FARMLAND && !land.hasFlag(Flag.FARMLAND_GRIEF)) {
			e.setCancelled(true);
			return;
		}

		if(block.getType() == Material.ARMOR_STAND && !land.isBypassing(player, Action.ARMOR_STAND_INTERACT)){
			e.setCancelled(true);
			return;
		}

		if(e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {

			if(land.getType() == LandType.SYSTEM && land.hasFlag(Flag.DOORS_AUTOCLOSE) && (e.getClickedBlock().getType().toString().endsWith("_DOOR") || e.getClickedBlock().getType().toString().endsWith("_GATE"))) {
				if(block.getBlockData() instanceof Openable) {
					Openable openable = (Openable) block.getBlockData();
					if(!openable.isOpen() && !land.isBypassing(e.getPlayer(), Action.ALL)) {
						new BukkitRunnable() {
							@Override
							public void run() {
								Openable o = (Openable) block.getBlockData();
								if(o.isOpen()) {
									o.setOpen(false);
									block.setBlockData(o);
									block.getState().update();
								}
							}
						}.runTaskLater(plugin, 60L);
					}
				}
				return;
			}

			if((block.getType() == Material.ANVIL && !land.isBypassing(player, Action.USE_ANVIL))
					|| (block.getType() == Material.BREWING_STAND && !land.isBypassing(player, Action.BREWING_STAND_INTERACT))
					|| ((block.getBlockData() instanceof Powerable && block.getType() != Material.LECTERN) && !land.isBypassing(player, Action.USE))
					|| (block.getBlockData() instanceof Bed && !land.isBypassing(player, Action.USE_BED))
					|| ((block.getType() == Material.DRAGON_EGG || (block.getType().name().startsWith("POTTED_") || block.getType() == Material.FLOWER_POT)) && !land.isBypassing(player, Action.OTHER_INTERACTS))
					|| (((block.getState() instanceof InventoryHolder && block.getType() != Material.LECTERN) || block.getType() == Material.JUKEBOX) && !land.isBypassing(player, Action.OPEN_CONTAINER))
					|| (block.getType() == Material.LECTERN && !land.isBypassing(player, Action.LECTERN_READ))
					|| ((player.getInventory().getItemInMainHand().getType().toString().contains("SPAWN_EGG") || player.getInventory().getItemInOffHand().getType().toString().contains("SPAWN_EGG")) && !land.isBypassing(player, Action.OTHER_INTERACTS))) {
				e.setCancelled(true);
			}
		}else if(e.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
			if(block.getType() == Material.DRAGON_EGG && !land.isBypassing(player, Action.OTHER_INTERACTS)){
				e.setCancelled(true);
			}
		}else if(e.getAction() == org.bukkit.event.block.Action.PHYSICAL && !land.isBypassing(player, Action.PHYSICAL_INTERACT)){
			e.setCancelled(true);
		}

	}

	@EventHandler
	public void onEntityInteract(EntityInteractEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());
		if (land != null && e.getEntityType() != EntityType.VILLAGER) {
			if (land.hasFlag(Flag.PRESSURE_PLATE_BY_ENTITY)) {
				if (e.getBlock() != null) {
					switch (e.getBlock().getType()) {
						case ACACIA_PRESSURE_PLATE:
						case OAK_PRESSURE_PLATE:
						case DARK_OAK_PRESSURE_PLATE:
						case JUNGLE_PRESSURE_PLATE:
						case BIRCH_PRESSURE_PLATE:
						case CRIMSON_PRESSURE_PLATE:
						case WARPED_PRESSURE_PLATE:
						case STONE_PRESSURE_PLATE:
						case SPRUCE_PRESSURE_PLATE:
						case LIGHT_WEIGHTED_PRESSURE_PLATE:
						case POLISHED_BLACKSTONE_PRESSURE_PLATE:
							return;
					}
				}
			}
			e.setCancelled(true);
		}
	}


	@EventHandler
	public void onFerilize(BlockFertilizeEvent e) {
		Player player = e.getPlayer();
		
		if(player == null) return;
		
		Block block = e.getBlock();

		Land land = landmanager.getLandAt(block.getLocation());

		if(!land.isBypassing(player, Action.OTHER_INTERACTS)) {
			e.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteractAtEntity(PlayerInteractEntityEvent e) {
		Land land = landmanager.getLandAt(e.getRightClicked().getLocation());

		if(land == null) {
			return;
		}
		if(!land.isBypassing(e.getPlayer(), Action.ENTITY_INTERACT)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
		Land land = landmanager.getLandAt(e.getRightClicked().getLocation());
		if(land == null) {
			return;
		}
		if(!land.isBypassing(e.getPlayer(), Action.ENTITY_INTERACT)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLeash(PlayerLeashEntityEvent e) {
		Player player = e.getPlayer();
		Land land = landmanager.getLandAt(e.getEntity().getLocation());

		if(land == null) return;

		if(!land.isBypassing(player, Action.LEASH)) {
			e.setCancelled(true);
		}
	}
}
