package fr.iban.lands.events;

import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLandEnterEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;
    private final Land fromLand;
    private final Land toLand;
    private final Player player;

    public PlayerLandEnterEvent(@NotNull Player player, @NotNull Land fromLand, @NotNull Land toLand) {
        this.isCancelled = false;
        this.player = player;
        this.fromLand = fromLand;
        this.toLand = toLand;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Land getFromLand() {
        return fromLand;
    }

    public Land getToLand() {
        return toLand;
    }

    public Player getPlayer() {
        return player;
    }
}
