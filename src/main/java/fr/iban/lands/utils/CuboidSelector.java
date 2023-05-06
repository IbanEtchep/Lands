package fr.iban.lands.utils;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.land.SubLand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CuboidSelector {

	private final Player player;

	private Location pos1;
	private Location pos2;

	private final Runnable quitCallback;
	private BukkitTask particleTask;

	private final LandManager manager;
	private final SubLand land;

	public CuboidSelector(Player player, SubLand land, LandManager manager, Runnable quitCallback) {
		this.land = land;
		this.manager = manager;
		this.player = player;
		this.quitCallback = quitCallback;
		Cuboid currentCuboid = land.getCuboid();
		if(land.getCuboid() != null) {
			this.pos1 = currentCuboid.getLowerNE();
			this.pos2 = currentCuboid.getUpperSW();
			showParticules(currentCuboid);
		}
	}

	public void startSelecting() {
		player.sendMessage("§c§lAttention, le sous-territoire doit se trouver dans votre territoire pour fonctionner.");
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
					if(valid) {
						cancelTask();
						land.setCuboid(cuboid, LandsPlugin.getInstance().getServerName());
						manager.saveSubLandCuboid(land);
						player.sendMessage("§a§lLa selection a été sauvegardée avec succès.");
						Bukkit.getScheduler().runTask(plugin, quitCallback);
						plugin.getTextInputs().remove(player.getUniqueId());
					}
				});
			}else if(texte.startsWith("quit")){
				cancelTask();
				quitCallback.run();
				plugin.getTextInputs().remove(player.getUniqueId());
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


	private Component getHelpText() {
		Component pos1 = Component.text("§6§lpos1")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour définir la position 1")))
				.clickEvent(ClickEvent.runCommand("/sayinchat pos1"));
		Component pos2 = Component.text("§6§lpos2")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour définir la position 2")))
				.clickEvent(ClickEvent.runCommand("/sayinchat pos2"));
		Component claim = Component.text("§a§lsauvegarder")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour claim la sélection")))
				.clickEvent(ClickEvent.runCommand("/sayinchat sauvegarder"));
		Component quit = Component.text("§c§lquitter")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour quitter le mode sélection")))
				.clickEvent(ClickEvent.runCommand("/sayinchat quit"));

		return Component.text("§e§lLes mots clés pour la création d'une sélection sont (hover-clic possible) : ")
				.append(pos1)
				.append(Component.text(" §e§l, ", NamedTextColor.YELLOW, TextDecoration.BOLD))
				.append(pos2)
				.append(Component.text(" §e§l, ", NamedTextColor.YELLOW, TextDecoration.BOLD))
				.append(claim)
				.append(Component.text(" §e§l, ", NamedTextColor.YELLOW, TextDecoration.BOLD))
				.append(quit)
				.append(Component.text(".", NamedTextColor.RED, TextDecoration.BOLD));
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
		List<Location> edgeLocations = cuboid.getCubeEdgeLocations(0.5);

		if(particleTask != null) {
			particleTask.cancel();
		}

		particleTask = Bukkit.getScheduler().runTaskTimer(LandsPlugin.getInstance(), () -> {
			for (Location location : edgeLocations) {
				showParticle(location);
			}
		},1L, 10L);
	}

	private void showParticle(Location loc) {
		player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
	}

}
