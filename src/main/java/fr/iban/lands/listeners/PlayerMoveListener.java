package fr.iban.lands.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.events.LandEnterEvent;
import fr.iban.lands.objects.Land;

public class PlayerMoveListener implements Listener {


	private LandManager landmanager;
	private LandsPlugin plugin;

	public PlayerMoveListener(LandsPlugin plugin) {
		this.landmanager = plugin.getLandManager();
		this.plugin = plugin;
	}


	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		final Location from = e.getFrom();
		final Location to = e.getTo();


		int x = Math.abs(from.getBlockX() - to.getBlockX());
		int y = Math.abs(from.getBlockY() - to.getBlockY());
		int z = Math.abs(from.getBlockZ() - to.getBlockZ());

		if (x == 0 && y == 0 && z == 0) return;


		//onMoveBlock :

//		Chunk cfrom = from.getBlock().getChunk();
//		Chunk cto = to.getBlock().getChunk();
//
//		int cx = cfrom.getX() - cto.getX();
//		int cz = cfrom.getZ() - cto.getZ();
//
//		if(cx == 0 && cz == 0) return;


//		//onMoveChunk

		landmanager.future(() -> {
			Land lfrom = landmanager.getLandAt(from);
			Land lto = landmanager.getLandAt(to);
			
			LandEnterEvent enter = new LandEnterEvent(player, lfrom, lto);
			
			Bukkit.getScheduler().runTask(plugin, () -> {
				Bukkit.getPluginManager().callEvent(enter);
				
				if(enter.isCancelled()) {
					player.teleportAsync(from);
					return;
				}
			});
		});
	}
}
