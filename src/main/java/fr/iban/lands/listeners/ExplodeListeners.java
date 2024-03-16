package fr.iban.lands.listeners;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class ExplodeListeners implements Listener {

    private final LandsPlugin plugin;
    private final LandRepository landRepository;

    public ExplodeListeners(LandsPlugin landsPlugin) {
        this.landRepository = landsPlugin.getLandRepository();
        this.plugin = landsPlugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Player player = getTargetPlayer(event);

        Iterator<Block> it = event.blockList().iterator();

        while (it.hasNext()) {
            Block block = it.next();
            Land land = landRepository.getLandAt(block.getLocation());

            if (player != null) {
                if (land.isBypassing(player, Action.BLOCK_BREAK)
                        && plugin.getConfig().getBoolean("enable-creeper-grief")) {
                    continue;
                }
            }

            if (!land.hasFlag(Flag.EXPLOSIONS)) {
                it.remove();
            }
        }
    }

    private Player getTargetPlayer(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            if (creeper.getTarget() instanceof Player player) {
                return player;
            }
        }

        return null;
    }
}
