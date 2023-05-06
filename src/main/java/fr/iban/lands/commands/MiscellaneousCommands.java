package fr.iban.lands.commands;

import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public class MiscellaneousCommands {

    @Command("sayinchat")
    public void sayInChat(Player player, String message) {
        player.chat(message);
    }

}
