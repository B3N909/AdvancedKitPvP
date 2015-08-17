package me.savant.pvp;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import me.savant.pvp.IconMenu.OptionClickEventHandler;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ListenerMethods implements Listener
{	
	KitClass[] kits;
	Main plugin;
	Economy econ;
	public ListenerMethods(KitClass[] kits, Main plugin, Economy econ){
		this.kits = kits;
		this.plugin = plugin;
		this.econ = econ;
	}
	
	@EventHandler
	public void command(PlayerCommandPreprocessEvent e)
	{
		//TODO: Full Plugin
		//Bukkit.getLogger().info("Please Purchase the Full Plugin");
		//if(true)
		//{
		//	return;
		//}
		if(e.getMessage().contains("/kit-open"))
		{
			String name = e.getMessage().substring(e.getMessage().lastIndexOf("-") + 1);
			IconMenu ui = new IconMenu(name, 9, new IconMenu.OptionClickEventHandler(){
				@Override
				public void onOptionClick(me.savant.pvp.IconMenu.OptionClickEvent e) {
					
					for(KitClass kit : kits)
					{
						if(kit.getName().equalsIgnoreCase(ChatColor.stripColor(e.getName())))
						{
							Player p = e.getPlayer();
							if(kit.getPermission() == null) 
							{
								Bukkit.getLogger().info("Kit " + kit.getName() + " has no permission");
								return;
							}
							if(e.getPlayer().hasPermission(kit.getPermission()))
							{
								if(kit.getItems() == null)
								{
									Bukkit.getLogger().info("Kit " + kit.getName() + " has no items to give");
									return;
								}
								for(ItemStack item : kit.getItems())
									p.getInventory().addItem(item);
								p.sendMessage(ChatColor.GREEN + "Redeemed kit " + kit.getName());
							}
							else
							{
								p.sendMessage(ChatColor.RED + "You do not have permission for kit " + kit.getName() + " : ( " + kit.getPermission() + " )");
							}
						}
					}
					
					e.getPlayer().closeInventory();
				}
			}, plugin);
			int i = 0;
			if(kits == null)
			{
				Bukkit.getLogger().info("No Kits Found");
				return;
			}
			for(KitClass kit : kits)
			{
				if(kit.getGUI().equalsIgnoreCase(name))
				{
					ui.setOption(i, kit.getGUIItem(), ChatColor.GOLD + kit.getName(), "Click to equip!");
					i++;
				}
			}
			ui.open(e.getPlayer());
			e.setCancelled(true);
		}
		if(e.getMessage().equalsIgnoreCase("/kit-reload"))
		{
			Player p = e.getPlayer();
			if(p.isOp())
			{
				float time = System.currentTimeMillis();
				plugin.generateKits();
				kits = new KitClass(plugin).getKits();
				p.sendMessage(ChatColor.GREEN + "Reloaded in " + (float)(System.currentTimeMillis() - time) + "mil");
				e.setCancelled(true);
			}
		}
		if(e.getMessage().equalsIgnoreCase("/kit-generate-default"))
		{
			Player p = e.getPlayer();
			if(p.isOp())
			{
				ItemStack[] items = new ItemStack[3];
				ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
				sword.addUnsafeEnchantment(Enchantment.DURABILITY, 2);
				ItemMeta im = sword.getItemMeta();
				im.setDisplayName(ChatColor.GOLD + "Default Sword");
				sword.setItemMeta(im);
				items[0] = sword;
				items[1] = new ItemStack(Material.COOKED_BEEF, 64);
				items[2] = new ItemStack(Material.MUSHROOM_SOUP, 8);
				plugin.getConfig().set("kit1.name", String.valueOf("default"));
				plugin.getConfig().set("kit1.permission", String.valueOf("advpvp.kit.default"));
				plugin.getConfig().set("kit1.items", items);
				plugin.getConfig().set("kit1.guiname", String.valueOf("default"));
				plugin.getConfig().set("kit1.guiitem", new ItemStack(Material.DIAMOND_AXE, 1));
				plugin.getConfig().set("kit1.cost", Integer.valueOf(0));
				plugin.saveConfig();
				p.sendMessage(ChatColor.GREEN + "Generated Default Kit");
				e.setCancelled(true);
			}
		}
		if(kits == null) return;
		for(KitClass kit : kits)
		{
			if(e.getMessage().equalsIgnoreCase("/kit-" + kit.getName()))
			{
				Player p = e.getPlayer();
				if(kit.getPermission() == null) 
				{
					Bukkit.getLogger().info("Kit " + kit.getName() + " has no permission");
					return;
				}
				if(econ.getBalance(p) - kit.getCost() < 0)
				{
					p.sendMessage(ChatColor.DARK_RED + "You need "  + ChatColor.RED + kit.getCost() + "$" + ChatColor.DARK_RED + " to get this kit!");
					e.setCancelled(true);
					return;
				}
				if(e.getPlayer().hasPermission(kit.getPermission()))
				{
					if(kit.getItems() == null)
					{
						Bukkit.getLogger().info("Kit " + kit.getName() + " has no items to give");
						return;
					}
					for(ItemStack item : kit.getItems())
						p.getInventory().addItem(item);
					p.sendMessage(ChatColor.DARK_RED + "Redeemed kit " + ChatColor.RED + kit.getName() + ChatColor.DARK_RED + " for " + ChatColor.RED + kit.getCost() + "$");
					econ.withdrawPlayer(p, (double)kit.getCost());
				}
				else
				{
					p.sendMessage(ChatColor.RED + "You do not have permission for kit " + kit.getName() + " : ( " + kit.getPermission() + " )");
				}
				e.setCancelled(true);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(e.getItem() != null && e.getItem().getType().equals(Material.MUSHROOM_SOUP))
			{
				if(p.getItemInHand().getAmount() > 2)
					p.setItemInHand(new ItemStack(Material.MUSHROOM_SOUP, p.getItemInHand().getAmount() - 1));
				else
					p.setItemInHand(new ItemStack(Material.AIR));
				p.setHealth(20D);
				p.updateInventory();
				e.setCancelled(true);
			}
			else
			{
				if(e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName())
				{
					String name = ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName());
					if(name.equalsIgnoreCase("Member Kits"))
					{
						String name1 = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Member Kits";
						IconMenu ui = new IconMenu(name1, 9, new IconMenu.OptionClickEventHandler(){
							@Override
							public void onOptionClick(me.savant.pvp.IconMenu.OptionClickEvent e) {
								
								for(KitClass kit : kits)
								{
									if(kit.getName().equalsIgnoreCase(ChatColor.stripColor(e.getName())))
									{
										Player p = e.getPlayer();
										if(kit.getPermission() == null) 
										{
											Bukkit.getLogger().info("Kit " + kit.getName() + " has no permission");
											return;
										}
										if(e.getPlayer().hasPermission(kit.getPermission()))
										{
											if(kit.getItems() == null)
											{
												Bukkit.getLogger().info("Kit " + kit.getName() + " has no items to give");
												return;
											}
											for(ItemStack item : kit.getItems())
												p.getInventory().addItem(item);
											p.sendMessage(ChatColor.GREEN + "Redeemed kit " + kit.getName());
										}
										else
										{
											p.sendMessage(ChatColor.RED + "You do not have permission for kit " + kit.getName() + " : ( " + kit.getPermission() + " )");
										}
									}
								}
								
								e.getPlayer().closeInventory();
							}
						}, plugin);
						int i = 0;
						if(kits == null)
						{
							Bukkit.getLogger().info("No Kits Found");
							return;
						}
						for(KitClass kit : kits)
						{
							if(kit.getGUI().equalsIgnoreCase("member"))
							{
								ui.setOption(i, kit.getGUIItem(), ChatColor.GOLD + kit.getName(), "Click to equip!");
								i++;
							}
						}
						ui.open(e.getPlayer());
					}
					else if(name.equalsIgnoreCase("Donator Kits"))
					{
						String name1 = ChatColor.YELLOW + "" + ChatColor.BOLD + "Donator Kits";
						IconMenu ui = new IconMenu(name1, 9, new IconMenu.OptionClickEventHandler(){
							@Override
							public void onOptionClick(me.savant.pvp.IconMenu.OptionClickEvent e) {
								
								for(KitClass kit : kits)
								{
									if(kit.getName().equalsIgnoreCase(ChatColor.stripColor(e.getName())))
									{
										Player p = e.getPlayer();
										if(kit.getPermission() == null) 
										{
											Bukkit.getLogger().info("Kit " + kit.getName() + " has no permission");
											return;
										}
										if(e.getPlayer().hasPermission(kit.getPermission()))
										{
											if(kit.getItems() == null)
											{
												Bukkit.getLogger().info("Kit " + kit.getName() + " has no items to give");
												return;
											}
											for(ItemStack item : kit.getItems())
												p.getInventory().addItem(item);
											p.sendMessage(ChatColor.GREEN + "Redeemed kit " + kit.getName());
										}
										else
										{
											p.sendMessage(ChatColor.RED + "You do not have permission for kit " + kit.getName() + " : ( " + kit.getPermission() + " )");
										}
									}
								}
								
								e.getPlayer().closeInventory();
							}
						}, plugin);
						int i = 0;
						if(kits == null)
						{
							Bukkit.getLogger().info("No Kits Found");
							return;
						}
						for(KitClass kit : kits)
						{
							if(kit.getGUI().equalsIgnoreCase("donator"))
							{
								ui.setOption(i, kit.getGUIItem(), ChatColor.GOLD + kit.getName(), "Click to equip!");
								i++;
							}
						}
						ui.open(e.getPlayer());
					}
				}
				
				if(e.getClickedBlock() != null && e.getClickedBlock().getState() instanceof Sign)
				{
					Sign s = (Sign) e.getClickedBlock().getState();
					if(ChatColor.stripColor(s.getLine(1)).equalsIgnoreCase("Click for..."))
					{
						String name = ChatColor.stripColor(s.getLine(2));
						name = name.replace("[", "");
						name = name.replace("]", "");
						
						for(KitClass kit : kits)
						{
							if(kit.getName().equalsIgnoreCase(name))
							{
								if(kit.getPermission() == null) 
								{
									Bukkit.getLogger().info("Kit " + kit.getName() + " has no permission");
									return;
								}
								if(econ.getBalance(p) - kit.getCost() < 0)
								{
									p.sendMessage(ChatColor.DARK_RED + "You need "  + ChatColor.RED + kit.getCost() + "$" + ChatColor.DARK_RED + " to get this kit!");
									e.setCancelled(true);
									return;
								}
								if(e.getPlayer().hasPermission(kit.getPermission()))
								{
									if(kit.getItems() == null)
									{
										Bukkit.getLogger().info("Kit " + kit.getName() + " has no items to give");
										return;
									}
									for(ItemStack item : kit.getItems())
										p.getInventory().addItem(item);
									p.sendMessage(ChatColor.DARK_RED + "Redeemed kit " + ChatColor.RED + kit.getName() + ChatColor.DARK_RED + " for " + ChatColor.RED + kit.getCost() + "$");
									econ.withdrawPlayer(p, (double)kit.getCost());
									p.updateInventory();
								}
								else
								{
									p.sendMessage(ChatColor.RED + "You do not have permission for kit " + kit.getName() + " : ( " + kit.getPermission() + " )");
								}
								e.setCancelled(true);
							}
						}
						
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent  e)
	{
		if(e.getItemDrop().getItemStack().hasItemMeta())
		{
			if(e.getItemDrop().getItemStack().getItemMeta().hasLore())
			{
				if(e.getItemDrop().getItemStack().getItemMeta().getLore().contains("KitPvP-NoDrop"))
				{
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e)
	{
		if(e.getDrops() != null)
		{
			List<ItemStack> drops = e.getDrops();
			ListIterator<ItemStack> litr = drops.listIterator();
			while(litr.hasNext())
			{
				ItemStack l = litr.next();
				if(l.hasItemMeta() && l.getItemMeta().hasLore() && l.getItemMeta().getLore().contains("KitPvP-NoDrop"))
				{
					litr.remove();
				}
			}
		}
	}
	
	@EventHandler
	public void onFood(FoodLevelChangeEvent e)
	{
		e.setFoodLevel(20);
	}
	
	@EventHandler
	public void onSign(SignChangeEvent e)
	{
		if(e.getLine(0).equalsIgnoreCase("[advpvp]"))
		{
			if(!(e.getLine(0).equalsIgnoreCase("")))
			{
				if(kits != null)
				{
					for(KitClass kit : kits)
					{
						if(kit.getName().equalsIgnoreCase(e.getLine(1)))
						{
							e.setLine(0, "");
							e.setLine(1, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Click for...");
							e.setLine(2, ChatColor.GREEN + "" + ChatColor.BOLD + "[" + kit.getName() + "]");
							if(kit.getCost() == 0)
							{
								e.setLine(3, ChatColor.RED + "FREE!");
							}
							else
							{
								e.setLine(3, ChatColor.RED + "" + kit.getCost() + "$");
							}
							return;
						}
					}
					e.setLine(0, ChatColor.DARK_RED + "ERROR");
					e.setLine(1, ChatColor.RED + "Kit not found!");
					
				}
			}
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if(p.hasPermission("advpvp.gui.default"))
		{
			ItemStack item = new ItemStack(Material.COAL, 1);
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Member Kits");
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + "Right click to view all of the member kits!");
			im.setLore(lore);
			item.setItemMeta(im);
			p.getInventory().addItem(item);
		}
		if(p.hasPermission("advpvp.gui.donator"))
		{
			ItemStack item = new ItemStack(Material.DIAMOND, 1);
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Donator Kits");
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + "" + ChatColor.MAGIC + "Right_click_to_view_all_of_the_donator_kits!");
			lore.add(ChatColor.YELLOW + "Right click to view all of the donator kits!");
			lore.add(ChatColor.YELLOW + "" + ChatColor.MAGIC + "Right_click_to_view_all_of_the_donator_kits!");
			im.setLore(lore);
			item.setItemMeta(im);
			p.getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e)
	{
		Player p = e.getPlayer();
		if(p.hasPermission("advpvp.gui.default"))
		{
			ItemStack item = new ItemStack(Material.COAL, 1);
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Member Kits");
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + "Right click to view all of the member kits!");
			im.setLore(lore);
			item.setItemMeta(im);
			p.getInventory().addItem(item);
		}
		if(p.hasPermission("advpvp.gui.donator"))
		{
			ItemStack item = new ItemStack(Material.DIAMOND, 1);
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Donator Kits");
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + "" + ChatColor.MAGIC + "Right_click_to_view_all_of_the_donator_kits!");
			lore.add(ChatColor.YELLOW + "Right click to view all of the donator kits!");
			lore.add(ChatColor.YELLOW + "" + ChatColor.MAGIC + "Right_click_to_view_all_of_the_donator_kits!");
			im.setLore(lore);
			item.setItemMeta(im);
			p.getInventory().addItem(item);
		}
	}
	
}
