package fr.iban.lands.commands;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;

import java.util.concurrent.CompletableFuture;

import org.bukkit.OfflinePlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class MaxClaimsCommand {

    private final LandManager landManager;

    public MaxClaimsCommand(LandsPlugin plugin) {
        this.landManager = plugin.getLandManager();
    }

    @Command("addmaxclaim")
    @CommandPermission("lands.admin")
    public void addMaxClaim(
            BukkitCommandActor player, OfflinePlayer target, @Range(min = 1) int amount) {
        landManager.increaseChunkLimit(target.getUniqueId(), amount);
        player.reply(
                "§7Nombre maximum de claims de §8" + target.getName() + "§7 augmenté de §8" + amount);
    }

    @Command("removemaxclaim")
    @CommandPermission("lands.admin")
    public void removeMaxClaim(
            BukkitCommandActor player, OfflinePlayer target, @Range(min = 1) int amount) {
        landManager.decreaseChunkLimit(target.getUniqueId(), amount);
        player.reply(
                "§7Nombre maximum de claims de §8" + target.getName() + "§7 diminué de §8" + amount);
    }

    @Command("getMaxClaim")
    @CommandPermission("lands.admin")
    public void getMaxClaim(BukkitCommandActor sender, OfflinePlayer target) {
        CompletableFuture<Integer> maxClaims = landManager.getMaxChunkCount(target.getUniqueId());
        maxClaims.thenAccept(
                count ->
                        sender.reply("§7Nombre maximum de claims de §8" + target.getName() + "§7: §8" + count));
    }
}
