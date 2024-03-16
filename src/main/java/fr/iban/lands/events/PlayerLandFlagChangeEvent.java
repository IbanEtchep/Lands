package fr.iban.lands.events;

import fr.iban.lands.enums.Flag;
import fr.iban.lands.model.land.Land;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLandFlagChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Flag flag;
    private final boolean newState;
    private boolean isCancelled;
    private final Land land;
    private final Player player;

    public PlayerLandFlagChangeEvent(@NotNull Player player, @NotNull Land land, Flag flag, boolean newState) {
        this.isCancelled = false;
        this.player = player;
        this.land = land;
        this.flag = flag;
        this.newState = newState;
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

    public Land getLand() {
        return land;
    }

    public Flag getFlag() {
        return flag;
    }

    public boolean getNewState() {
        return newState;
    }

    public Player getPlayer() {
        return player;
    }
}
