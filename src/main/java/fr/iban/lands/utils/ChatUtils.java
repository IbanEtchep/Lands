package fr.iban.lands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatUtils {

    private ChatUtils() {}

    public static String toPlainText(Component newName) {
        return PlainTextComponentSerializer.plainText().serialize(newName);
    }

    public static String toMiniMessage(Component newName) {
        return MiniMessage.miniMessage().serialize(newName);
    }
}
