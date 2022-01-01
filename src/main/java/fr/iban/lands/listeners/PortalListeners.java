package fr.iban.lands.listeners;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class PortalListeners implements Listener {

	private LandManager manager;

	public PortalListeners(LandsPlugin plugin) {
		this.manager = plugin.getLandManager();
	}
	
	@EventHandler
	public void onPortalCreate(PortalCreateEvent e) {
		if(e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			
			if(e.getReason() == CreateReason.NETHER_PAIR) {
				for(BlockState block : e.getBlocks()) {
					Land land = manager.getLandAt(block.getLocation());
					if(!land.isBypassing(player, Action.BLOCK_PLACE)) {
						e.setCancelled(true);
						player.sendMessage("§cLa création du portail a été annulée, celui-ci se trouvant sur un claim où vous n'êtes pas autorisé à poser des blocs.");
						return;
					}
				}
			}
		}
	}
}
