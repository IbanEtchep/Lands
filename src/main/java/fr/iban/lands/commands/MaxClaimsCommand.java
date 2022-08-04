package fr.iban.lands.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.AccountManager;
import fr.iban.common.data.Account;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.core.BukkitActor;


public class MaxClaimsCommand {

    @Command("addmaxclaim")
    @CommandPermission("lands.admin")
    public void addMaxClaim(BukkitCommandActor player, OfflinePlayer target, @Range(min = 1) int amount) {
        AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
        Account account = accountManager.getAccount(target.getUniqueId());
        account.addMaxClaims((short) amount);
        accountManager.saveAccountAsync(account);
    }

    @Command("removemaxclaim")
    @CommandPermission("lands.admin")
    public void removeMaxClaim(BukkitCommandActor player, OfflinePlayer target, @Range(min = 1) int amount) {
        AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
        Account account = accountManager.getAccount(target.getUniqueId());
        account.removeMaxClaims((short) amount);
        accountManager.saveAccountAsync(account);
    }

    @Command("getMaxClaim")
    @CommandPermission("lands.admin")
    public void getMaxClaim(BukkitCommandActor sender, OfflinePlayer target) {
        AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
        Account account = accountManager.getAccount(target.getUniqueId());
        sender.reply("§7Nombre maximum de claims : §8" + account.getMaxClaims());
    }

}
