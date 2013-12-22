package com.sparkedia.valrix.evercart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class CartCommandExecutor implements CommandExecutor {

	private PluginManager plm;
	private FileConfiguration config;
	private ConfigurationSection carts;

	public static final String ALL = "all";
	public static final String ON = "on";
	public static final String OFF = "off";
	public static final String STORAGE = "storage";
	public static final String RIDEABLE = "rideable";
	public static final String COMMAND = "command";
	public static final String EXPLOSIVE = "explosive";
	public static final String HOPPER = "hopper";
	public static final String POWERED = "powered";
	public static final String SPAWNER = "spawner";
	public static final String ACTIVE = "active";

	private static final List<String> types = new ArrayList<String>();
	public static final Map<String, Class<? extends Minecart>> keyToClass = new HashMap<String, Class<? extends Minecart>>();

	static {
		types.add(RIDEABLE);
		keyToClass.put(RIDEABLE, RideableMinecart.class);
		types.add(POWERED);
		keyToClass.put(POWERED, PoweredMinecart.class);
		types.add(STORAGE);
		keyToClass.put(STORAGE, StorageMinecart.class);
		types.add(HOPPER);
		keyToClass.put(HOPPER, HopperMinecart.class);
		types.add(EXPLOSIVE);
		keyToClass.put(EXPLOSIVE, ExplosiveMinecart.class);
		types.add(COMMAND);
		keyToClass.put(COMMAND, CommandMinecart.class);
		types.add(SPAWNER);
		keyToClass.put(SPAWNER, SpawnerMinecart.class);
	}

	public CartCommandExecutor(Plugin plugin) {
		this.plm = plugin.getServer().getPluginManager();
		this.config = plugin.getConfig();
		this.carts = this.config.getConfigurationSection("evercart");
		if (this.carts == null) {
			this.carts = this.config.createSection("evercart");
			toggle(STORAGE);
		} else {
			init();
		}
	}

	private void init() {
		for (String type : types) {
			if (carts.getBoolean(type)) {
				toggle(type);
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (sender instanceof Player && !sender.isOp()
				&& sender.hasPermission("evercart")) {
			return false;
		}
		if (args.length == 0 || !isValid(args[0])) {
			usage(sender);
			return true;
		}
		if (args[0].equals(ACTIVE)) {
			showActive(sender);
			return true;
		} else if (args[0].equals(ALL)) {
			if (args.length < 2
					|| (!args[1].equals(ON) && !args[1].equals(OFF))) {
				usage(sender);
				return true;
			}
			all(args[1].equals(ON));
			showActive(sender);
		} else if (args.length == 1) {
			toggle(args[0]);
			showActive(args[0], sender);
		} else {
			Boolean on = null;
			if (args[1].equals(ON)) {
				on = true;
			}
			if (args[1].equals(OFF)) {
				on = false;
			}
			if (on == null) {
				usage(sender);
				return true;
			} else {
				toggle(args[0], on);
			}
			showActive(args[0], sender);
		}
		return true;
	}

	private boolean isValid(String type) {
		return type.equals(ALL) || type.equals(ACTIVE) || types.contains(type);
	}

	private void all(boolean on) {
		for (String type : types) {
			toggle(type, on);
		}
	}

	private void toggle(String type) {
		toggle(type, !getStatus(type));
	}

	private boolean getStatus(String type) {
		return carts.getBoolean(type);
	}

	private void toggle(String type, boolean on) {
		carts.set(type, on);
		plm.callEvent(new CartEvent(keyToClass.get(type), on));
	}

	private void showActive(CommandSender sender) {
		List<String> msgs = new ArrayList<String>();
		msgs.add(ChatColor.DARK_PURPLE + "Active minecart types for evercart:");
		List<String> active = new ArrayList<String>();
		for (String type : types) {
			if (getStatus(type)) {
				active.add(type);
				msgs.add("\t" + ChatColor.WHITE + type);
			}
		}
		if (active.isEmpty()) {
			msgs.add("\t" + ChatColor.WHITE + "none");
		}
		sender.sendMessage(msgs.toArray(new String[0]));
	}

	private void showActive(String type, CommandSender sender) {
		boolean on = carts.getBoolean(type);
		sender.sendMessage(ChatColor.DARK_PURPLE + (on ? "A" : "Dea")
				+ "ctivated type " + ChatColor.WHITE + type);
	}

	private void usage(CommandSender sender) {
		List<String> msgs = new ArrayList<String>();
		msgs.add(ChatColor.DARK_PURPLE + "Usage of evercart:");
		msgs.add(ChatColor.WHITE + "/evercart all <on|off>" + ChatColor.YELLOW
				+ "Toggle all minecart types on or off.");
		msgs.add(ChatColor.WHITE + "/evercart <type> [on|off]"
				+ ChatColor.YELLOW
				+ " Toggle the specified minecart type to be affected.");
		msgs.add(ChatColor.YELLOW + " Possible types are: " + ChatColor.WHITE
				+ types);
		msgs.add(ChatColor.WHITE + "/evercart active" + ChatColor.YELLOW
				+ " List all active types.");
		sender.sendMessage(msgs.toArray(new String[0]));
	}
}
