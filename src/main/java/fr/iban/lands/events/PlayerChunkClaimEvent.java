package fr.iban.lands.events;

import fr.iban.lands.model.SChunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerChunkClaimEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;
    private final Player player;
    private final SChunk chunk;

    public PlayerChunkClaimEvent(@NotNull Player player, SChunk chunk) {
        this.isCancelled = false;
        this.player = player;
        this.chunk = chunk;
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

    public Player getPlayer() {
        return player;
    }

    public SChunk getChunk() {
        return chunk;
    }
}
