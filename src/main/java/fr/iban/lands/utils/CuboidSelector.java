package fr.iban.lands.utils;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.SubLand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CuboidSelector {

	private Player player;

	private Location pos1;
	private Location pos2;

	private SelectionCallback callback;
	private BukkitTask particleTask;

	private LandManager manager;
	private SubLand land;

	public CuboidSelector(Player player, SubLand land, LandManager manager, SelectionCallback callback) {
		this.land = land;
		this.manager = manager;
		this.player = player;
		this.callback = callback;
	}

	public void startSelecting() {
		player.sendMessage(getHelpText());
		CoreBukkitPlugin plugin = CoreBukkitPlugin.getInstance();
		plugin.getTextInputs().put(player.getUniqueId(), texte -> {

			if(texte.equalsIgnoreCase("pos1")){
				cancelTask();
				pos1 = player.getLocation();
				player.sendMessage("§a§lPosition 1 définie.");	

				Cuboid cuboid = getCuboid();
				if(cuboid != null) {
					showParticules(cuboid);
				}

			}else if(texte.equalsIgnoreCase("pos2")){
				cancelTask();
				pos2 = player.getLocation();
				player.sendMessage("§a§lPosition 2 définie.");

				Cuboid cuboid = getCuboid();
				if(cuboid != null) {
					showParticules(cuboid);
				}

			}else if(texte.equalsIgnoreCase("sauvegarder")){
				Cuboid cuboid = getCuboid();
				verif(cuboid).thenAcceptAsync(valid -> {
					if(valid.booleanValue()) {
						land.setCuboid(cuboid, CoreBukkitPlugin.getInstance().getServerName());
						manager.saveSubLandCuboid(land);
						player.sendMessage("§a§lLa selection a été sauvegardée avec succès.");
						callback.quit();
						plugin.getTextInputs().remove(player.getUniqueId());
						cancelTask();
					}
				});
			}else if(texte.startsWith("quit")){
				callback.quit();
				plugin.getTextInputs().remove(player.getUniqueId());
				cancelTask();
			}else {
				player.sendMessage("§c§lLe texte que vous avez saisi est invalide.");
				startSelecting();
			}
		});
	}

	private void cancelTask() {
		if(particleTask != null) {
			particleTask.cancel();
		}
	}


	private BaseComponent[] getHelpText() {
		BaseComponent[] pos1 = new ComponentBuilder("§6§lpos1").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour définir la position 1")).event(ChatUtils.getCommandClickEvent("pos1")).create();
		BaseComponent[] pos2 = new ComponentBuilder("§6§lpos2").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour définir la position 2")).event(ChatUtils.getCommandClickEvent("pos2")).create();
		BaseComponent[] claim = new ComponentBuilder("§a§lsauvegarder").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour claim la sélection")).event(ChatUtils.getCommandClickEvent("sauvegarder")).create();
		BaseComponent[] quit = new ComponentBuilder("§c§lquitter").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour quitter le mode sélection")).event(ChatUtils.getCommandClickEvent("quit")).create();
		
		ComponentBuilder builder = new ComponentBuilder("§e§lLes mots clés pour la création d'une sélection sont (hover-clic possible) : ")
				.append(pos1).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null)
				.append(pos2).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null)
				.append(claim).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null);

		builder.append(quit).append(TextComponent.fromLegacyText(".")).event((HoverEvent)null);
		
		return builder.create();
	}

	private boolean arePosSet() {
		return pos1 != null && pos2 != null;
	}

	private Cuboid getCuboid() {
		if(!arePosSet()) {
			return null;
		}
		return new Cuboid(pos1, pos2);
	}

	private CompletableFuture<Boolean> verif(Cuboid cuboid) {
		player.sendMessage("§a§lVérification de la sélection...");
		return manager.future(() -> {
			if(cuboid == null) {
				if(pos1 == null) {
					player.sendMessage("§c§lLa position 1 n'a pas encore été définie !");
				}else {
					player.sendMessage("§c§lLa position 2 n'a pas encore été définie !");
				}
				return false;
			}
			
			if(manager.getLandAt(cuboid.getUpperSW().getChunk()) != manager.getLandAt(cuboid.getLowerNE().getChunk())) {
				player.sendMessage("§c§lLa selection doit se trouver dans le territoire " + land.getSuperLand().getName() + ".");
				return false;
			}
			
			return true;
		});
	}

	private void showParticules(Cuboid cuboid) {
		Location loc1 = cuboid.getLowerNE();
		Location loc2 = cuboid.getUpperSW();
		Location loc3 = new Location(cuboid.getWorld(), loc1.getX(), 0, loc2.getZ());
		Location loc4 = new Location(cuboid.getWorld(), loc2.getX(), 0, loc1.getZ());
		particleTask = new BukkitRunnable() {

			@Override
			public void run() {
				for (int y = loc1.getBlockY(); y < loc2.getBlockY(); y++) {
					showParticle(loc1, y);
					showParticle(loc2, y);
					showParticle(loc3, y);
					showParticle(loc4, y);
				}			
			}
		}.runTaskTimer(LandsPlugin.getInstance(), 1L, 10L);

	}

	private void showParticle(Location loc, int y) {
		loc.setY(y);
		player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
	}

}
