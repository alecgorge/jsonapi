package com.alecgorge.minecraft.jsonapi.stringifier;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class BukkitStringifier {
	public static HashMap<String, Class<?>> handle = new HashMap<String, Class<?>>();

	static {
		handle.put("Player", org.bukkit.entity.Player.class);
		handle.put("Player[]", org.bukkit.entity.Player[].class);
		handle.put("OfflinePlayer", org.bukkit.OfflinePlayer.class);
		handle.put("OfflinePlayer[]", org.bukkit.OfflinePlayer[].class);
		handle.put("Server", org.bukkit.Server.class);
		handle.put("World", org.bukkit.World.class);
		handle.put("World[]", org.bukkit.World[].class);
		handle.put("Plugin", org.bukkit.plugin.Plugin.class);
		handle.put("Plugin[]", org.bukkit.plugin.Plugin[].class);
		handle.put("ItemStack", org.bukkit.inventory.ItemStack.class);
		handle.put("File", java.io.File.class);
		handle.put("ItemStack[]", org.bukkit.inventory.ItemStack[].class);
		handle.put("PlayerInventory", org.bukkit.inventory.PlayerInventory.class);
		handle.put("Inventory", org.bukkit.inventory.Inventory.class);
		handle.put("Location", org.bukkit.Location.class);
		handle.put("World.Environment", org.bukkit.World.Environment.class);
		handle.put("GameMode", org.bukkit.GameMode.class);
		handle.put("Enchantment", org.bukkit.enchantments.Enchantment.class);
		handle.put("Block", org.bukkit.block.Block.class);
		handle.put("Object[]", java.lang.Object[].class);

		if (JSONAPI.instance.getServer().getPluginManager().getPlugin("Vault") != null) {
			handle.put("EconomyResponse", net.milkbowl.vault.economy.EconomyResponse.class);
		}
	}

	public static boolean canHandle(Class<?> c) {
		for (Class<?> cc : handle.values()) {
			if (cc.isAssignableFrom(c)) {
				return true;
			}
		}
		return false;
	}

	public static Object handle(Object obj) {
		if (obj instanceof World.Environment) {
			World.Environment e = (World.Environment) obj;
			if (e == World.Environment.NETHER) {
				return "nether";
			} else {
				return "normal";
			}
		} else if (obj instanceof File) {
			return ((File) obj).toString();
		} else if (obj instanceof Block) {
			Block b = (Block) obj;
			JSONObject o = new JSONObject();

			o.put("type", b.getTypeId());
			o.put("data", b.getData());

			return o;
		} else if (obj instanceof Player) {
			Player p = (Player) obj;
			JSONObject o = new JSONObject();

			o.put("name", p.getName());
			o.put("op", p.isOp());
			o.put("health", p.getHealth());
			o.put("foodLevel", p.getFoodLevel());
			o.put("exhaustion", p.getExhaustion());
			o.put("ip", p.getAddress() != null ? p.getAddress().toString() : "offline");
			o.put("itemInHand", p.getItemInHand());
			o.put("location", p.getLocation());
			o.put("inventory", p.getInventory());
			// o.put("enderchest", p.getEnderChest());
			o.put("sneaking", p.isSneaking());
			o.put("sprinting", p.isSprinting());
			o.put("inVehicle", p.isInsideVehicle());
			o.put("sleeping", p.isSleeping());
			o.put("world", p.getServer().getWorlds().indexOf(p.getWorld()));
			o.put("worldInfo", p.getWorld());
			o.put("gameMode", p.getGameMode());
			o.put("banned", p.isBanned());
			o.put("whitelisted", p.isWhitelisted());

			o.put("level", p.getLevel());
			o.put("experience", p.getTotalExperience());
			o.put("firstPlayed", Math.round(p.getFirstPlayed() / 1000.0));
			o.put("lastPlayed", Math.round(p.getLastPlayed() / 1000.0));
			o.put("enderchest", p.getEnderChest());

			return o;
		} else if (obj instanceof OfflinePlayer) {
			OfflinePlayer op = (OfflinePlayer) obj;
			JSONObject o = new JSONObject();

			Player target = JSONAPI.loadOfflinePlayer(op.getName());
			if (target != null)
				return target;

			o.put("firstPlayed", Math.round(op.getFirstPlayed() / 1000.0));
			o.put("lastPlayed", Math.round(op.getLastPlayed() / 1000.0));
			o.put("banned", op.isBanned());
			o.put("whitelisted", op.isWhitelisted());
			o.put("name", op.getName());

			return o;
		} else if (obj instanceof Server) {
			Server s = (Server) obj;

			JSONObject o = new JSONObject();

			o.put("maxPlayers", s.getMaxPlayers());
			o.put("players", Arrays.asList(s.getOnlinePlayers()));
			o.put("port", s.getPort());
			o.put("name", s.getName());
			o.put("serverName", s.getServerName());
			o.put("version", s.getVersion());
			o.put("worlds", s.getWorlds());

			return o;
		} else if (obj instanceof World) {
			World w = (World) obj;

			JSONObject o = new JSONObject();

			o.put("environment", w.getEnvironment());
			o.put("fullTime", w.getFullTime());
			o.put("time", w.getTime());
			o.put("name", w.getName());
			o.put("isThundering", w.isThundering());
			o.put("hasStorm", w.hasStorm());
			o.put("remainingWeatherTicks", w.getWeatherDuration());

			return o;
		} else if (obj instanceof Plugin) {
			Plugin p = (Plugin) obj;
			PluginDescriptionFile d = p.getDescription();

			JSONObject o = new JSONObject();

			o.put("name", d.getName());
			o.put("description", d.getDescription());
			o.put("authors", d.getAuthors());
			o.put("version", d.getVersion());
			o.put("website", d.getWebsite());
			o.put("enabled", JSONAPI.instance.getServer().getPluginManager().isPluginEnabled(p));

			return o;
		} else if (obj instanceof ItemStack) {
			ItemStack i = (ItemStack) obj;

			JSONObject o = new JSONObject();

			o.put("type", i.getTypeId());
			o.put("durability", i.getDurability());
			o.put("dataValue", (int) i.getData().getData());
			o.put("amount", i.getAmount());

			JSONObject enchantments = new JSONObject();
			for (Map.Entry<Enchantment, Integer> enchantment : i.getEnchantments().entrySet()) {
				enchantments.put(enchantment.getKey().getId(), enchantment.getValue());
			}

			o.put("enchantments", enchantments);
			
			if (((ItemStack) obj).getType().equals(Material.BOOK_AND_QUILL) || ((ItemStack) obj).getType().equals(Material.WRITTEN_BOOK)) {
				JSONObject book = new JSONObject();
				
				BookMeta bookObj = (BookMeta)((ItemStack)obj).getItemMeta();
				
				book.put("pages", bookObj.getPages());
				book.put("title", bookObj.getTitle());
				book.put("author", bookObj.getAuthor());
				
				o.put("book", book);
			}
			
			return o;
		} else if (obj instanceof PlayerInventory) {
			PlayerInventory p = (PlayerInventory) obj;

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
		} else if (obj instanceof Inventory) {
			Inventory p = (Inventory) obj;

			return Arrays.asList(p.getContents());
		} else if (obj instanceof Location) {
			Location l = (Location) obj;

			JSONObject o = new JSONObject();

			o.put("x", l.getX());
			o.put("y", l.getY());
			o.put("z", l.getZ());
			o.put("pitch", l.getPitch());
			o.put("yaw", l.getYaw());

			return o;
		} else if (obj instanceof Plugin[]) {
			List<Plugin> l = Arrays.asList((Plugin[]) obj);

			Collections.sort(l, new PluginSorter());

			return l;
		} else if (obj instanceof GameMode) {
			return ((GameMode) obj).getValue();
		} else if (obj instanceof Enchantment) {
			return ((Enchantment) obj).getId();
		} else if (JSONAPI.instance.getServer().getPluginManager().getPlugin("Vault") != null && obj instanceof EconomyResponse) {
			JSONObject o = new JSONObject();
			EconomyResponse r = (EconomyResponse) obj;

			o.put("amount", r.amount);
			o.put("balance", r.balance);
			o.put("errorMessage", r.errorMessage);
			o.put("type", r.type.toString());

			return o;
		} else if (obj instanceof Object[]) {
			int l = ((Object[]) obj).length;
			JSONArray a = new JSONArray();
			for (int i = 0; i < l; i++) {
				a.add(((Object[]) obj)[i]);
			}

			return a;
		}
		Logger.getLogger("JSONAPI").warning("Uncaugh object! Value:");
		Logger.getLogger("JSONAPI").warning(obj.toString());
		Logger.getLogger("JSONAPI").warning("Type:");
		Logger.getLogger("JSONAPI").warning(obj.getClass().getName());

		return new Object();
	}

	static class PluginSorter implements Comparator<Plugin> {
		@Override
		public int compare(Plugin o1, Plugin o2) {
			return o1.getDescription().getName().compareTo(o2.getDescription().getName());
		}
	}
}
