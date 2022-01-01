package fr.iban.lands.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class BlockPlaceListener implements Listener {


	private LandManager landmanager;

	public BlockPlaceListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}


	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());

		if(land != null && !land.isBypassing(e.getPlayer(), Action.BLOCK_PLACE)) {
			e.setCancelled(true);
		}
	}



	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());

		if(land != null && !land.isBypassing(e.getPlayer(), Action.BUCKET_EMPTY)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketFillEvent e) {
		Block block = e.getBlock();
		Land land = landmanager.getLandAt(block.getLocation());

		if(land != null && !land.isBypassing(e.getPlayer(), Action.BUCKET_FILL)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityBlockForm(EntityBlockFormEvent e){
		if(e.getEntity() instanceof Player){
			Player player = (Player) e.getEntity();
			Block block = e.getBlock();
			Land land = landmanager.getLandAt(block.getLocation());

			if(!land.isBypassing(player, Action.FROST_WALK)){
				e.setCancelled(true);
			}
		}
	}
}
