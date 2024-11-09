package fr.iban.lands.task;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.model.land.Land;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class PotionEffectTask implements Runnable {

    private final LandsPlugin plugin;
    
    public PotionEffectTask(LandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            Land land = plugin.getLandRepository().getLandAt(player.getLocation());

            for(PotionEffect effect : land.getEffects()) {
                if(player.getActivePotionEffects().stream().noneMatch(potionEffect ->
                        potionEffect.getType().equals(effect.getType()) && potionEffect.getAmplifier() > effect.getAmplifier())
                ) {
                    player.addPotionEffect(effect);
                }
            }
        });
    }
}
