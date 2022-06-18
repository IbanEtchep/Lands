package fr.iban.lands.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.AccountManager;
import fr.iban.common.data.Account;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MaxClaimsCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(sender.hasPermission("lands.admin")) {
			
			switch (label.toLowerCase()) {
			case "addmaxclaim":
				if(args.length == 2) {
					@SuppressWarnings("deprecation")
					OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
					int nombre = Integer.parseInt(args[1]);
					AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
					Account account = accountManager.getAccount(op.getUniqueId());
					account.addMaxClaims((short) nombre);
					accountManager.saveAccountAsync(account);
				}
				break;
			case "removemaxclaim":
				if(args.length == 2) {
					@SuppressWarnings("deprecation")
					OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
					int nombre = Integer.parseInt(args[1]);
					AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
					Account account = accountManager.getAccount(op.getUniqueId());
					account.removeMaxClaims((short) nombre);
					accountManager.saveAccountAsync(account);
				}
				break;
			case "getmaxclaim":
				if(args.length == 1) {
					@SuppressWarnings("deprecation")
					OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
					AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
					Account account = accountManager.getAccount(op.getUniqueId());
					sender.sendMessage("§7Nombre maximum de claims : §8" + account.getMaxClaims());
					}
				break;
			default:
				break;
			}

		}

		return false;
	}



}

