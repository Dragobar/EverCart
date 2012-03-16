package com.sparkedia.valrix.evercart;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EverCart extends JavaPlugin {
	private CartListener cl;
	public Logger log;
	private String pName;
	
	public void onDisable() {
		PluginDescriptionFile pdf = getDescription();
		log.info('['+pName+"]: v"+pdf.getVersion()+" has been disabled.");
	}
	
	public void onEnable() {
		PluginDescriptionFile pdf = getDescription();
		pName = pdf.getName();
		log = getServer().getLogger();
		cl = new CartListener(this);
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(cl, this);
		
		log.info('['+pName+"]: v"+pdf.getVersion()+" has been enabled.");
	}
}
