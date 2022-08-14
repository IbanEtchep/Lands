package fr.iban.lands.listeners;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Set;

public class BlockBreakListener implements Listener {


	private LandManager landmanager;
	private final Set<Material> cropList = EnumSet.of(
			Material.WHEAT, Material.POTATOES, Material.CARROTS,
			Material.BEETROOTS, Material.NETHER_WART );
	private LandsPlugin plugin;

	public BlockBreakListener(LandsPlugin landsPlugin) {
		this.plugin = landsPlugin;
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());

		if(land == null) return;

		if(land.hasFlag(Flag.AUTO_REPLANT)) {
			Material material = block.getType();

			if (material == Material.SUGAR_CANE && block.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
				return;
			}

			if(cropList.contains(material)) {
				BlockData bd = block.getBlockData();
				Ageable age = (Ageable) bd;

				if(age.getAge() == age.getMaximumAge()) {

					new BukkitRunnable() {

						@Override
						public void run() {

							block.setType(material);
						}
					}.runTaskLater(plugin, 1L);
					return;
				}
			}
		}

		if(!land.isBypassing(e.getPlayer(), Action.BLOCK_BREAK)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleBreak(VehicleDestroyEvent e) {
		if (e.getAttacker() instanceof Player) {
			Land land = landmanager.getLandAt(e.getVehicle().getLocation());
			if(!land.isBypassing((Player) e.getAttacker(), Action.VEHICLE_PLACE_BREAK)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		if(e.getHitBlock() == null) {
			return;
		}

		if(e.getEntity().getShooter() instanceof Player player) {
			Land land = landmanager.getLandAt(e.getHitBlock().getLocation());

			if(!land.isBypassing(player, Action.BLOCK_BREAK)) {
				e.setCancelled(true);
			}
		}

	}

}
