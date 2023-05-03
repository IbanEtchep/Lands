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
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
						callback.quit();
						plugin.getTextInputs().remove(player.getUniqueId());
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


	private Component getHelpText() {
		Component pos1 = Component.text("§6§lpos1")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour définir la position 1")))
				.clickEvent(ClickEvent.suggestCommand("pos1"));
		Component pos2 = Component.text("§6§lpos2")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour définir la position 2")))
				.clickEvent(ClickEvent.suggestCommand("pos2"));
		Component claim = Component.text("§a§lsauvegarder")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour claim la sélection")))
				.clickEvent(ClickEvent.suggestCommand("sauvegarder"));
		Component quit = Component.text("§c§lquitter")
				.hoverEvent(HoverEvent.showText(Component.text("§aCliquez pour quitter le mode sélection")))
				.clickEvent(ClickEvent.suggestCommand("quit"));

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
		double particleDistance = 0.5;
		Location corner1 = cuboid.getLowerNE();
		Location corner2 = cuboid.getUpperSW().clone().add(1, 1, 1);
		List<Location> result = new ArrayList<>();
		World world = corner1.getWorld();
		double minX = Math.min(corner1.getX(), corner2.getX());
		double minY = Math.min(corner1.getY(), corner2.getY());
		double minZ = Math.min(corner1.getZ(), corner2.getZ());
		double maxX = Math.max(corner1.getX(), corner2.getX());
		double maxY = Math.max(corner1.getY(), corner2.getY());
		double maxZ = Math.max(corner1.getZ(), corner2.getZ());

		for (double x = minX; x <= maxX; x+=particleDistance) {
			for (double y = minY; y <= maxY; y+=particleDistance) {
				for (double z = minZ; z <= maxZ; z+=particleDistance) {
					int components = 0;
					if (x == minX || x == maxX) components++;
					if (y == minY || y == maxY) components++;
					if (z == minZ || z == maxZ) components++;
					if (components >= 2) {
						result.add(new Location(world, x, y, z));
					}
				}
			}
		}
		particleTask = new BukkitRunnable() {

			@Override
			public void run() {
				for (Location location : result) {
					showParticle(location);
				}
			}
		}.runTaskTimer(LandsPlugin.getInstance(), 1L, 10L);

	}

	private void showParticle(Location loc) {
		player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
	}

}
