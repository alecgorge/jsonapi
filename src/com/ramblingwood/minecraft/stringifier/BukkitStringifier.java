package com.ramblingwood.minecraft.stringifier;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simpleForBukkit.JSONObject;

public class BukkitStringifier {
	public static HashMap<String, Class<?>> handle = new HashMap<String, Class<?>>();
	
	static {
		handle.put("Player", org.bukkit.entity.Player.class);
		handle.put("Player[]", org.bukkit.entity.Player[].class);
		handle.put("Server", org.bukkit.Server.class);
		handle.put("World", org.bukkit.World.class);
		handle.put("World[]", org.bukkit.World[].class);
		handle.put("Plugin", org.bukkit.plugin.Plugin.class);
		handle.put("Plugin[]", org.bukkit.plugin.Plugin[].class);
		handle.put("ItemStack", org.bukkit.inventory.ItemStack.class);
		handle.put("ItemStack[]", org.bukkit.inventory.ItemStack[].class);
		handle.put("PlayerInventory", org.bukkit.inventory.PlayerInventory.class);
		handle.put("Location", org.bukkit.Location.class);
		handle.put("World.Environment", org.bukkit.World.Environment.class);
	}
	
	public static boolean canHandle(Class<?> c) {
		for(Class<?> cc : handle.values()) {
			// if (c instance of cc)
			if(cc.isAssignableFrom(c)) {
				return true;
			}
		}
		return false;
	}
	
	public static Object handle(Object obj) {
		if(obj instanceof World.Environment) {
			World.Environment e = (World.Environment)obj;
			if(e == World.Environment.NETHER) {
				return "nether";
			}
			else {
				return "normal";
			}
		}
		else if(obj instanceof Player) {
			Player p = (Player)obj;
			JSONObject o = new JSONObject();

			o.put("name", p.getName());
			o.put("op", p.isOp());
			o.put("health", p.getHealth());
			o.put("ip", p.getAddress().toString());
			o.put("itemInHand", p.getItemInHand());
			o.put("location", p.getLocation());
			o.put("inventory", p.getInventory());
			o.put("sneaking", p.isSneaking());
			o.put("inVehicle", p.isInsideVehicle());
			
			return o;			
		}
		else if(obj instanceof Server) {
			Server s = (Server)obj;
			
			JSONObject o = new JSONObject();
			
			o.put("maxPlayers", s.getMaxPlayers());
			o.put("players", Arrays.asList(s.getOnlinePlayers()));
			o.put("port", s.getPort());
			o.put("name", s.getName());
			o.put("serverName", s.getServerName());
			o.put("version", s.getVersion());
			o.put("worlds", s.getWorlds());
			
			return o;
		}
		else if(obj instanceof World) {
			World w = (World)obj;
			
			JSONObject o = new JSONObject();
			
			o.put("environment", w.getEnvironment());
			o.put("fullTime", w.getFullTime());
			o.put("time", w.getTime());
			o.put("name", w.getName());
			
			return o;
		}
		else if(obj instanceof Plugin) {
			Plugin p = (Plugin)obj;
			PluginDescriptionFile d = p.getDescription();
			
			JSONObject o = new JSONObject();
			
			o.put("name", d.getName());
			o.put("description", d.getDescription());
			o.put("authors", d.getAuthors());
			o.put("version", d.getVersion());
			
			return o;
		}
		else if(obj instanceof ItemStack) {
			ItemStack i = (ItemStack)obj;
			
			JSONObject o = new JSONObject();
			
			o.put("type", i.getTypeId());
			o.put("maxSize", i.getMaxStackSize());
			o.put("durability", i.getDurability());
			o.put("amount", i.getAmount());
			
			return o;
		}
		else if(obj instanceof PlayerInventory) {
			PlayerInventory p = (PlayerInventory)obj;
			
			JSONObject o = new JSONObject();
			
			JSONObject armor = new JSONObject();
			armor.put("boots", p.getBoots());
			armor.put("chestplate", p.getChestplate());
			armor.put("helmet", p.getHelmet());
			armor.put("leggings", p.getLeggings());
			
			o.put("armor", armor);
			o.put("hand", p.getItemInHand());
			o.put("inventory", Arrays.asList(p.getContents()));
			
			return o;
		}
		else if(obj instanceof Location) {
			Location l = (Location)obj;
			
			JSONObject o = new JSONObject();
			
			o.put("x", l.getBlockX());
			o.put("y", l.getBlockY());
			o.put("z", l.getBlockZ());
			o.put("pitch", l.getPitch());
			o.put("yaw", l.getYaw());
			
			return o;
		}
		
		return new Object();
	}
}
