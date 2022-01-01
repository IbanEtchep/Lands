package fr.iban.lands.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import fr.iban.lands.objects.Land;

public class LandEnterEvent extends Event implements Cancellable{

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;
	private Land fromLand;
	private Land toLand;
	private Player player;
	
	public LandEnterEvent(@NotNull Player player, @NotNull Land fromLand, @NotNull Land toLand) {
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
