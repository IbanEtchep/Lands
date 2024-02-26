package fr.iban.lands.utils;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.land.*;

import java.util.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class LandMap {

    private final LandManager manager;
    private final Map<UUID, Land> landMapSelection = new HashMap<>();

    public LandMap(LandManager manager) {
        this.manager = manager;
    }

    public void display(Player player, Land land) {
        landMapSelection.put(player.getUniqueId(), land);
        List<Component> components = get(player, 15, 5, land);
        for (Component component : components) {
            player.sendMessage(component);
        }
    }

    public List<Component> get(Player player, int rangeX, int rangeZ, Land land) {
        List<Component> components = new ArrayList<>();
        String servername = LandsPlugin.getInstance().getServerName();
        final Chunk center = player.getChunk();
        final World world = center.getWorld();

        final int startX = center.getX() - rangeX;
        final int startZ = center.getZ() - rangeZ;
        final int endX = center.getX() + rangeX + 1;
        final int endZ = center.getZ() + rangeZ + 1;

        for (int i = 0; i < rangeZ * 2 + 1; i++) {
            components.add(Component.empty());
        }

        for (int x = startX; x < endX; x++) {
            int i = 0;
            for (int z = startZ; z < endZ; z++) {
                SChunk chunkXZ = new SChunk(servername, world.getName(), x, z);
                components.set(i, components.get(i).append(getSymbole(player, center, chunkXZ, land)));
                i++;
            }
        }

        Component headerAndFooter = Component.text("█".repeat(rangeX * 2 + 1), NamedTextColor.BLACK);
        components.add(0, headerAndFooter);

        return components;
    }

    private Component getSymbole(Player player, Chunk center, SChunk schunk, Land selectedLand) {
        TextColor baseColor = TextColor.fromHexString(HexColor.MARRON_CLAIR.getHex());
        Component baseComponent = Component.text("█").color(baseColor);

        Land land = manager.getLandAt(schunk);

        if (land instanceof SystemLand sland) {
            TextColor systemLandColor = TextColor.fromHexString(HexColor.MARRON.getHex());
            Component hoverComponent =
                    Component.text(sland.getName()).color(systemLandColor).decorate(TextDecoration.BOLD);
            if (land.getName().equals("Zone sauvage")) {
                if (selectedLand != null) {
                    hoverComponent =
                            hoverComponent.append(
                                    Component.text("\n(clic pour claim)").color(NamedTextColor.GRAY));
                    baseComponent =
                            baseComponent.clickEvent(
                                    ClickEvent.runCommand(
                                            "/land claimat "
                                                    + schunk.getWorld()
                                                    + " "
                                                    + schunk.getX()
                                                    + " "
                                                    + schunk.getZ()));
                }
            } else {
                baseComponent = baseComponent.color(TextColor.fromHexString(HexColor.OLIVE.getHex()));
                if (!sland.equals(selectedLand)) {
                    baseComponent = baseComponent.clickEvent(ClickEvent.runCommand(""));
                } else {
                    baseComponent =
                            baseComponent.clickEvent(
                                    ClickEvent.runCommand(
                                            "/land unclaimat "
                                                    + schunk.getWorld()
                                                    + " "
                                                    + schunk.getX()
                                                    + " "
                                                    + schunk.getZ()));
                }
            }
            baseComponent = baseComponent.hoverEvent(HoverEvent.showText(hoverComponent));
        } else if (land instanceof PlayerLand pland) {
            UUID owner = pland.getOwner();
            if (Objects.equals(owner, player.getUniqueId())) {
                baseComponent = baseComponent.color(NamedTextColor.DARK_GREEN);
                Component hoverComponent =
                        Component.text("Territoire : " + pland.getName() + "\n")
                                .color(NamedTextColor.DARK_GREEN)
                                .append(Component.text("Propriétaire : Vous").color(NamedTextColor.DARK_GREEN));
                if (selectedLand != null) {
                    hoverComponent =
                            hoverComponent.append(
                                    Component.text("\n(clic pour unclaim)").color(NamedTextColor.GRAY));
                    baseComponent =
                            baseComponent.clickEvent(
                                    ClickEvent.runCommand(
                                            "/land unclaimat "
                                                    + schunk.getWorld()
                                                    + " "
                                                    + schunk.getX()
                                                    + " "
                                                    + schunk.getZ()));
                }
                baseComponent = baseComponent.hoverEvent(HoverEvent.showText(hoverComponent));
            } else {
                baseComponent = baseComponent.color(TextColor.fromHexString(HexColor.MARRON.getHex()));
                String ownerName = Bukkit.getOfflinePlayer(pland.getOwner()).getName();
                Component hoverComponent =
                        Component.text("Territoire : " + pland.getName() + "\n")
                                .color(TextColor.fromHexString(HexColor.MARRON.getHex()))
                                .append(
                                        Component.text("Propriétaire : " + ownerName)
                                                .color(TextColor.fromHexString(HexColor.MARRON.getHex())));
                baseComponent = baseComponent.hoverEvent(HoverEvent.showText(hoverComponent));
            }
        } else if (land instanceof GuildLand guildLand) {
            if (guildLand.isGuildMember(player.getUniqueId())) {
                baseComponent = baseComponent.color(NamedTextColor.AQUA);
                Component hoverComponent =
                        Component.text("Territoire de guilde : " + guildLand.getName() + "\n")
                                .color(NamedTextColor.DARK_GREEN)
                                .append(
                                        Component.text("Propriétaire : Guilde " + guildLand.getGuildName())
                                                .color(TextColor.fromHexString(HexColor.MARRON.getHex())));
                if (selectedLand != null) {
                    hoverComponent =
                            hoverComponent.append(
                                    Component.text("\n(clic pour unclaim)").color(NamedTextColor.GRAY));
                    baseComponent =
                            baseComponent.clickEvent(
                                    ClickEvent.runCommand(
                                            "/land unclaimat "
                                                    + schunk.getWorld()
                                                    + " "
                                                    + schunk.getX()
                                                    + " "
                                                    + schunk.getZ()));
                }
                baseComponent = baseComponent.hoverEvent(HoverEvent.showText(hoverComponent));
            } else {
                baseComponent = baseComponent.color(TextColor.fromHexString(HexColor.MARRON.getHex()));
                Component hoverComponent =
                        Component.text("Territoire de guilde : " + guildLand.getName() + "\n")
                                .color(TextColor.fromHexString(HexColor.MARRON.getHex()))
                                .append(
                                        Component.text("Propriétaire : Guilde " + guildLand.getGuildName())
                                                .color(TextColor.fromHexString(HexColor.MARRON.getHex())));
                baseComponent = baseComponent.hoverEvent(HoverEvent.showText(hoverComponent));
            }
        }

        if (center.getX() == schunk.getX() && center.getZ() == schunk.getZ()) {
            baseComponent = baseComponent.color(NamedTextColor.YELLOW);
        }

        return baseComponent;
    }

    public Map<UUID, Land> getLandMapSelection() {
        return landMapSelection;
    }
}
