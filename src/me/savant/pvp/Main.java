package me.savant.pvp;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener
{
	Plugin plugin;
	KitClass[] kits;
	Economy econ = null;
	public void onEnable()
	{
		plugin = Bukkit.getPluginManager().getPlugin("AdvancedKitPvP");
		PluginManager pm = Bukkit.getPluginManager();
		final ListenerMethods listen;
		
		pm.registerEvents(this, this);
		
		pm.registerEvents(new KitClass(this), this);
		
		if(!setupEconomy())
		{
			Bukkit.getLogger().info("[AdvancedPvP] Economy Error!");
		}
		
		//TODO: Full Plugin
		generateKits();
		//Bukkit.getLogger().info("Please Purchase the Full Plugin");
		
		listen = new ListenerMethods(kits, this, econ);
		
		pm.registerEvents(listen, this);
	}
	public void onDisable()
	{
		
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	public void generateKits()
	{
		//TODO: Full Plugin
		kits = new KitClass(this).getKits();
		//Bukkit.getLogger().info("Please Purchase the Full Plugin");
	}
}
