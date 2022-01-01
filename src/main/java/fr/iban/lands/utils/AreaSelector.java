package fr.iban.lands.utils;

import java.util.concurrent.CompletableFuture;

import fr.iban.lands.objects.SChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class AreaSelector {

	private Player player;

	private Location pos1;
	private Location pos2;

	private SelectionCallback callback;
	private BukkitTask particleTask;

	private LandManager manager;
	private Land land;


	public AreaSelector(Player player, Land land, LandManager manager, SelectionCallback callback) {
		this.land = land;
		this.manager = manager;
		this.player = player;
		this.callback = callback;
	}

	public void startSelecting() {
		player.sendMessage(getHelpText());
		CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
		core.getTextInputs().put(player.getUniqueId(), texte -> {

			if(texte.equalsIgnoreCase("pos1")){
				cancelTask();
				pos1 = player.getLocation();
				player.sendMessage("§a§lPosition 1 définie.");	

				Cuboid cuboid = getCuboid();
				if(cuboid != null) {
					player.sendMessage("§a§lVotre selection contient " + cuboid.getChunks().size() + " tronçons.");
					showParticules(cuboid);
				}

			}else if(texte.equalsIgnoreCase("pos2")){
				cancelTask();
				pos2 = player.getLocation();
				player.sendMessage("§a§lPosition 2 définie.");

				Cuboid cuboid = getCuboid();
				if(cuboid != null) {
					player.sendMessage("§a§lVotre selection contient " + cuboid.getChunks().size() + " tronçons.");
					showParticules(cuboid);
				}

			}else if(texte.equalsIgnoreCase("claim")){
				Cuboid cuboid = getCuboid();
				verif(cuboid, true).thenAcceptAsync(valid -> {
					if(valid.booleanValue()) {
						manager.claim(cuboid.getSChunks(), land, player);
					}
				});
			}else if(texte.equalsIgnoreCase("unclaim")){
				Cuboid cuboid = getCuboid();
				verif(cuboid, false).thenAcceptAsync(valid -> {
					if(valid.booleanValue()) {
						for(SChunk chunk : getCuboid().getSChunks()) {
							manager.unclaim(player, chunk, land, false);
						}
						player.sendMessage("§aLa selection a été unclaim avec succès.");
					}
				});
			}else if(player.hasPermission("lands.admin") && texte.equalsIgnoreCase("forceunclaim")) {
				if(arePosSet()) {
					for(Chunk chunk : getCuboid().getChunks()) {
						manager.unclaim(chunk);
					}
					player.sendMessage("§a§lLa selection a été unclaim avec succès.");
				}else {
					player.sendMessage("§c§lIl faut définir les deux positions !");
				}
			}else if(texte.startsWith("quit")){
				callback.quit();
				core.getTextInputs().remove(player.getUniqueId());
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
		BaseComponent[] claim = new ComponentBuilder("§a§lclaim").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour claim la sélection")).event(ChatUtils.getCommandClickEvent("claim")).create();
		BaseComponent[] unclaim = new ComponentBuilder("§c§lunclaim").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour unclaim la sélection")).event(ChatUtils.getCommandClickEvent("unclaim")).create();
		BaseComponent[] forceunclaim = new ComponentBuilder("§c§lforceunclaim").event(ChatUtils.getShowTextHoverEvent("§aCliquez TOUS les chunks de la sélection.")).event(ChatUtils.getCommandClickEvent("forceunclaim")).create();
		BaseComponent[] quit = new ComponentBuilder("§c§lquitter").event(ChatUtils.getShowTextHoverEvent("§aCliquez pour quitter le mode sélection")).event(ChatUtils.getCommandClickEvent("quit")).create();
		
		ComponentBuilder builder = new ComponentBuilder("§e§lLes mots clés pour la création d'une sélection sont (hover-clic possible) : ")
				.append(pos1).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null)
				.append(pos2).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null)
				.append(claim).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null)
				.append(unclaim).append(TextComponent.fromLegacyText(" §e§let ")).event((HoverEvent)null);

		
		if(player.hasPermission("lands.admin")) {
			builder.append(forceunclaim).append(TextComponent.fromLegacyText(" §e§l, ")).event((HoverEvent)null);
		}
		
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

	private CompletableFuture<Boolean> verif(Cuboid cuboid, boolean claim) {
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
			int unclaimCount = 0;
			int claimCount = 0;
			if(cuboid.getSizeX() > 3000 || cuboid.getSizeZ() > 3000){
				player.sendMessage("§c§lLa selection est trop grande !");
			}
			for(SChunk chunk : cuboid.getSChunks()) {
				Land chunkLand = manager.getLandAt(chunk);
				if(!chunkLand.isWilderness()) {
					if(!manager.getLandAt(chunk).equals(land)) {
						player.sendMessage("§c§lLa selection contient des tronçons qui ne sont pas vides et n'appartiennent pas au territoire " + land.getName() + ".");
						return false;
					}else {
						unclaimCount++;
					}
				}else {
					claimCount++;
				}
			}
			if(claim) {
				if(claimCount == 0) {
					player.sendMessage("§c§lIl n'y a pas de tronçons à claim dans la selection.");
					return false;
				}

				int remaining = manager.getRemainingChunkCount(player).get();
				if(claimCount > remaining) {
					player.sendMessage("§c§lVous essayez de claim " + claimCount + " tronçons alors qu'il ne vous en reste que " + remaining + " de libre.");
					return false;
				}

			}else {
				if(unclaimCount == 0) {
					player.sendMessage("§c§lIl n'y a pas de tronçons à unclaim dans la selection.");
					return false;
				}
			}

			return true;
		});
	}

	private void showParticules(Cuboid cuboid) {
		Location loc1 = cuboid.getLowerNE().getChunk().getBlock(0, 0, 0).getLocation().clone();
		Location loc2 = cuboid.getUpperSW().getChunk().getBlock(15, 0, 15).getLocation().add(1, 0, 1).clone();
		Location loc3 = new Location(cuboid.getWorld(), loc1.getX(), 0, loc2.getZ());
		Location loc4 = new Location(cuboid.getWorld(), loc2.getX(), 0, loc1.getZ());
		particleTask = new BukkitRunnable() {

			@Override
			public void run() {
				for (int y = 0; y < 256; y++) {
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
