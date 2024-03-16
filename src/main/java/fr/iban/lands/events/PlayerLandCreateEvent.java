package fr.iban.lands.events;

import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLandCreateEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final Land land;

    public PlayerLandCreateEvent(@NotNull Player player, Land land) {
        this.player = player;
        this.land = land;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Player getPlayer() {
        return player;
    }

    public Land getLand() {
        return land;
    }
}
