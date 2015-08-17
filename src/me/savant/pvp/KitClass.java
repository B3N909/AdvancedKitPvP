package me.savant.pvp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitClass implements Listener 
{
	Main plugin;
	public KitClass(Main plugin)
	{
		this.plugin = plugin;
	}
	
	public KitClass[] getKits()
	{
		List<KitClass> kits = new ArrayList<>();
		if(plugin.getConfig().getConfigurationSection("kit1") == null)
		{
			Bukkit.getLogger().info("Can't find 'kit1'?");
			return null;
		}
		
		int i = 1;
		for(@SuppressWarnings("unused") Object o : plugin.getConfig().getConfigurationSection("").getKeys(false))
		{
			ItemStack[] items = null;
			if(plugin.getConfig().get("kit" + i + ".items") != null)
			{
				List<?> list = plugin.getConfig().getList("kit" + i + ".items");
				items = new ItemStack[list.size()];
				int z = 0;
				for(Object o1 : list)
				{
					ItemStack item = (ItemStack) list.get(z);
					if(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName())
					{
						if(item.getItemMeta().getDisplayName().contains("&"))
						{
							ItemMeta im = item.getItemMeta();
							im.setDisplayName(ChatColor.translateAlternateColorCodes('&', im.getDisplayName()));
							item.setItemMeta(im);
						}
					}
					items[z] = item;
					z++;
				}
				//items = nerf(items);
			}
			else
				Bukkit.getLogger().info("No Items found for kit" + i);
			if(plugin.getConfig().getString("kit" + i + ".name").equalsIgnoreCase(""))
			{
				Bukkit.getLogger().info("No Kit name found for kit" + i);
				return null;
			}
			if(plugin.getConfig().getString("kit" + i + ".permission").equalsIgnoreCase(""))
			{
				Bukkit.getLogger().info("Not Kit permission found for kit" + i);
				return null;
			}
			if(plugin.getConfig().getString("kit" + i + ".guiname") == null)
			{
				Bukkit.getLogger().info("Not Kit Gui Name found for kit" + i);
				return null;
			}
			if(plugin.getConfig().getItemStack("kit" + i + ".guiitem") == null)
			{
				Bukkit.getLogger().info("Not Kit Gui Item found for kit" + i);
				return null;
			}
			kits.add(new KitClass(plugin.getConfig().getString("kit" + i + ".name"), plugin.getConfig().getString("kit" + i + ".permission"), plugin.getConfig().getString("kit" + i + ".guiname"), plugin.getConfig().getItemStack("kit" + i + ".guiitem"), items, plugin.getConfig().getInt("kit" + i + ".cost")));
			i++;
		}
		return castArrayKitClass(kits);
	}
	
	private KitClass[] castArrayKitClass(List<KitClass> list)
	{
		KitClass[] kits = new KitClass[list.toArray().length];
		int i = 0;
		for(KitClass l : list)
		{
			kits[i] = l;
			i++;
		}
		return kits;
	}
	
	private ItemStack[] nerf(ItemStack[] items)
	{
		ItemStack[] list = new ItemStack[items.length];
		int i = 0;
		for(ItemStack l : items)
		{
			ItemMeta im = l.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("KitPvP-NoDrop");
			im.setLore(lore);
			l.setItemMeta(im);
			list[i] = l;
			i++;
		}
		return list;
	}
	
	private String name;
	
	private String permission;
	
	private ItemStack[] contents;
	
	private String guiName;
	
	private ItemStack guiItem;
	
	private int cost;
	
	public KitClass(String name, String permission, String guiName, ItemStack guiItem, ItemStack[] contents, int cost)
	{
		this.name = name;
		this.permission = permission;
		this.contents = contents;
		this.guiItem = guiItem;
		this.guiName = guiName;
		this.cost = cost;
	}
	
	public String getPermission()
	{
		return permission;
	}
	
	public String getName()
	{
		return name;
	}
	
	public ItemStack[] getItems()
	{
		return contents;
	}
	
	public String getGUI()
	{
		return guiName;
	}
	
	public ItemStack getGUIItem()
	{
		return guiItem;
	}
	
	public int getCost()
	{
		return cost;
	}
}
