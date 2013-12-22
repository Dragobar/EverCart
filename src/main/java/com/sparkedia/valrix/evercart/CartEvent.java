package com.sparkedia.valrix.evercart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CartEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Class<? extends Minecart> type;
	private boolean on;

	public CartEvent(Class<? extends Minecart> type, boolean on) {
		this.type = type;
		this.on = on;
	}

	public Class<? extends Minecart> getType() {
		return type;
	}

	public boolean isActive() {
		return on;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
