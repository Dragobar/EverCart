package com.sparkedia.valrix.evercart;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class CartListener implements Listener {
	protected EverCart plugin;
	private ArrayList<Chunk> old = new ArrayList<Chunk>();

	private List<Class<? extends Minecart>> activeTypes = new ArrayList<Class<? extends Minecart>>();
	private Map<Minecart, Set<Chunk>> activeCarts = new ConcurrentHashMap<Minecart, Set<Chunk>>();
	private List<Chunk> activeChunks = new ArrayList<Chunk>();

	public CartListener(EverCart plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onCartToggle(CartEvent e) {
		if (e.isActive()) {
			activeTypes.add(e.getType());
		} else {
			removeCarts(e.getType());
		}
	}

	private void removeCarts(Class<? extends Minecart> type) {
		activeTypes.remove(type);
		Set<Minecart> remove = new HashSet<Minecart>();
		for (Minecart m : activeCarts.keySet()) {
			if (type.isAssignableFrom(m.getClass())) {
				unloadAll(activeCarts.get(m));
				remove.add(m);
			}
		}
		activeCarts.keySet().removeAll(remove);
	}

	private void unload(Chunk c) {
		activeChunks.remove(c);
		if (!activeChunks.contains(c)) {
			c.getWorld().unloadChunkRequest(c.getX(), c.getZ());
		}
	}

	private void unloadAll(Set<Chunk> remove) {
		for (Chunk c : remove) {
			unload(c);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleBlockCollision(VehicleBlockCollisionEvent e) {
		Vehicle v = e.getVehicle();
		if (!active(v)) {
			return;
		}
		unloadAll(activeCarts.remove(v));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleMove(VehicleMoveEvent e) {
		Vehicle v = e.getVehicle();
		if (!active(v)) {
			return;
		}
		Minecart m = (Minecart) v;
		// Get current chunk
		Chunk current = m.getLocation().getBlock().getChunk();
		// Set range that we want to keep the cart alive at.
		int range = 2;
		// Load in new chunks as we get to them.
		if (!activeCarts.containsKey(m)) {
			activeCarts.put(m, new HashSet<Chunk>());
		}
		Set<Chunk> chunks = activeCarts.get(m);
		int x = current.getX();
		int z = current.getZ();
		for (Iterator<Chunk> iter = chunks.iterator(); iter.hasNext();) {
			Chunk oc = iter.next();
			if (oc.getWorld() != current.getWorld() || oc.getX() > x + range
					|| oc.getX() < x - range || oc.getZ() > z + range
					|| oc.getZ() < z - range) {
				oc.getWorld().unloadChunkRequest(oc.getX(), oc.getZ());
				old.remove(oc);
			}
		}

		for (int dx = -(range); dx <= range; dx++) {
			for (int dz = -(range); dz <= range; dz++) {
				Chunk chunk = current.getWorld().getChunkAt(x + dx, z + dz);
				if (!chunks.contains(chunk)) {
					// Only load in chunks that are not already loaded
					chunk.getWorld().loadChunk(chunk);
					chunks.add(chunk);
					activeChunks.add(chunk);
				}
			}
		}
	}

	private boolean active(Vehicle v) {
		return activeTypes.contains(v.getClass());
	}
}
