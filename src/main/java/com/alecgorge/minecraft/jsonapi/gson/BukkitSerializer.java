package com.alecgorge.minecraft.jsonapi.gson;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.adminium.Adminium3.AdminiumPushNotification;
import com.alecgorge.minecraft.jsonapi.config.JSONAPIPermissionNode;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIGroup;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BukkitSerializer {
	private static GsonBuilder builder = new GsonBuilder();
	private static Gson gson = new Gson();
	
	static {
		builder.registerTypeAdapter(Player.class, new PlayerSerializer());
		builder.registerTypeAdapter(OfflinePlayer.class, new OfflinePlayerSerializer());
		builder.registerTypeAdapter(Server.class, new ServerSerializer());
		builder.registerTypeAdapter(World.class, new WorldSerializer());
		builder.registerTypeAdapter(Plugin.class, new PluginSerializer());
		builder.registerTypeAdapter(World.Environment.class, new WorldEnvironmentSerializer());
		builder.registerTypeAdapter(File.class, new FileSerializer());
		builder.registerTypeAdapter(Block.class, new BlockSerializer());
		builder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer());
		builder.registerTypeAdapter(PlayerInventory.class, new PlayerInventorySerializer());
		builder.registerTypeAdapter(Inventory.class, new InventorySerializer());
		builder.registerTypeAdapter(Location.class, new LocationSerializer());
		builder.registerTypeAdapter(Plugin[].class, new PluginArraySerializer());
		builder.registerTypeAdapter(JSONAPIUser.class, new JSONAPIUserSerializer());
		builder.registerTypeAdapter(JSONAPIGroup.class, new JSONAPIGroupSerializer());
		builder.registerTypeAdapter(JSONAPIPermissionNode.class, new JSONAPIPermissionNodeSerializer());
		builder.registerTypeAdapter(GameMode.class, new GameModeSerializer());
		builder.registerTypeAdapter(Enchantment.class, new EnchantmentSerializer());
		builder.registerTypeAdapter(EconomyResponse.class, new EconomyResponseSerializer());
		builder.registerTypeAdapter(AdminiumPushNotification.class, new AdminiumPushNotificationSerializer());
		builder.registerTypeAdapter(Date.class, new DateSerializer());
		
		gson = builder.create();
	}
	
	public static Gson getGson() {
		return gson;
	}
	
	public static String toJSON(Object o) {
		return getGson().toJson(o);
	}
	
	static class PluginSorter implements Comparator<Plugin> {
		@Override
		public int compare(Plugin o1, Plugin o2) {
			return o1.getDescription().getName().compareTo(o2.getDescription().getName());
		}
	}

	public static class PlayerSerializer implements JsonSerializer<Player> {
		@Override
		public JsonElement serialize(Player p, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("name", p.getName());
			o.addProperty("op", p.isOp());
			o.addProperty("health", p.getHealth());
			o.addProperty("foodLevel", p.getFoodLevel());
			o.addProperty("exhaustion", p.getExhaustion());
			o.addProperty("ip", p.getAddress() != null ? p.getAddress().toString() : "offline");
			o.add("itemInHand", getGson().toJsonTree(p.getItemInHand()));
			o.add("location", getGson().toJsonTree(p.getLocation()));
			o.add("inventory", getGson().toJsonTree(p.getInventory()));
			// o.put("enderchest", p.getEnderChest());
			o.addProperty("sneaking", p.isSneaking());
			o.addProperty("sprinting", p.isSprinting());
			o.addProperty("inVehicle", p.isInsideVehicle());
			o.addProperty("sleeping", p.isSleeping());
			o.addProperty("world", p.getServer().getWorlds().indexOf(p.getWorld()));
			o.add("worldInfo", getGson().toJsonTree(p.getWorld()));
			o.add("gameMode", getGson().toJsonTree(p.getGameMode()));
			o.addProperty("banned", p.isBanned());
			o.addProperty("whitelisted", p.isWhitelisted());

			o.addProperty("level", p.getLevel());
			o.addProperty("experience", p.getTotalExperience());
			o.addProperty("firstPlayed", Math.round(p.getFirstPlayed() / 1000.0));
			o.addProperty("lastPlayed", Math.round(p.getLastPlayed() / 1000.0));
			o.add("enderchest", getGson().toJsonTree(p.getEnderChest()));

			return o;
		}
	}

	public static class OfflinePlayerSerializer implements JsonSerializer<OfflinePlayer> {
		@Override
		public JsonElement serialize(OfflinePlayer op, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			Player target = JSONAPI.loadOfflinePlayer(op.getName());
			if (target != null)
				return getGson().toJsonTree(target);

			o.addProperty("firstPlayed", Math.round(op.getFirstPlayed() / 1000.0));
			o.addProperty("lastPlayed", Math.round(op.getLastPlayed() / 1000.0));
			o.addProperty("banned", op.isBanned());
			o.addProperty("whitelisted", op.isWhitelisted());
			o.addProperty("name", op.getName());

			return o;

		}
	}

	public static class ServerSerializer implements JsonSerializer<Server> {
		@Override
		public JsonElement serialize(Server s, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("maxPlayers", s.getMaxPlayers());
			o.add("players", getGson().toJsonTree(Arrays.asList(s.getOnlinePlayers())));
			o.addProperty("port", s.getPort());
			o.addProperty("name", s.getName());
			o.addProperty("serverName", s.getServerName());
			o.addProperty("version", s.getVersion());
			o.add("worlds", getGson().toJsonTree(s.getWorlds()));

			return o;

		}
	}

	public static class WorldSerializer implements JsonSerializer<World> {
		@Override
		public JsonElement serialize(World w, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.add("environment", getGson().toJsonTree(w.getEnvironment()));
			o.addProperty("fullTime", w.getFullTime());
			o.addProperty("time", w.getTime());
			o.addProperty("name", w.getName());
			o.addProperty("isThundering", w.isThundering());
			o.addProperty("hasStorm", w.hasStorm());
			o.addProperty("remainingWeatherTicks", w.getWeatherDuration());
			o.addProperty("isPVP", w.getPVP());
			o.addProperty("difficulty", w.getDifficulty().getValue());
			o.addProperty("seed", String.valueOf(w.getSeed()));
			
			List<String> playerNames = new ArrayList<String>();
			for(Player p : w.getPlayers()) {
				if(!p.getName().equals("¤fHerobrine")) {
					playerNames.add(p.getName());
				}
			}
			
			o.add("players", getGson().toJsonTree(playerNames));
			return o;

		}
	}

	public static class PluginSerializer implements JsonSerializer<Plugin> {
		@Override
		public JsonElement serialize(Plugin p, Type type, JsonSerializationContext context) {
			JSONAPI.dbug("using custom Plugin serializer");
			
			JsonObject o = new JsonObject();

			PluginDescriptionFile d = p.getDescription();

			o.addProperty("name", d.getName());
			o.addProperty("description", d.getDescription() == null ? "" : d.getDescription());
			o.add("authors", getGson().toJsonTree(d.getAuthors()));
			o.addProperty("version", d.getVersion());
			o.addProperty("website", d.getWebsite() == null ? "" : d.getWebsite());
			o.addProperty("enabled", JSONAPI.instance.getServer().getPluginManager().isPluginEnabled(p));
			o.add("commands", getGson().toJsonTree(d.getCommands()));

			return o;
		}
	}

	public static class WorldEnvironmentSerializer implements JsonSerializer<World.Environment> {
		@Override
		public JsonElement serialize(World.Environment e, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(e.name().toLowerCase());
		}
	}

	public static class FileSerializer implements JsonSerializer<File> {
		@Override
		public JsonElement serialize(File f, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(f.toString());
		}
	}

	public static class BlockSerializer implements JsonSerializer<Block> {
		@Override
		public JsonElement serialize(Block b, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("type", b.getTypeId());
			o.addProperty("data", b.getData());

			return o;
		}
	}

	public static class ItemStackSerializer implements JsonSerializer<ItemStack> {
		@Override
		public JsonElement serialize(ItemStack i, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("type", i.getTypeId());
			o.addProperty("durability", i.getDurability());
			o.addProperty("dataValue", (int) i.getData().getData());
			o.addProperty("amount", i.getAmount());

			JsonObject enchantments = new JsonObject();
			for (Map.Entry<Enchantment, Integer> enchantment : i.getEnchantments().entrySet()) {
				enchantments.addProperty(String.valueOf(enchantment.getKey().getId()), enchantment.getValue());
			}

			o.add("enchantments", getGson().toJsonTree(enchantments));
			
			if (i.getType().equals(Material.BOOK_AND_QUILL) || i.getType().equals(Material.WRITTEN_BOOK)) {
				JSONObject book = new JSONObject();
				
				BookMeta bookObj = (BookMeta)i.getItemMeta();
				
				book.put("pages", bookObj.getPages());
				book.put("title", bookObj.getTitle());
				book.put("author", bookObj.getAuthor());
				
				o.add("book", getGson().toJsonTree(book));
			}

			return o;
		}
	}

	public static class PlayerInventorySerializer implements JsonSerializer<PlayerInventory> {
		@Override
		public JsonElement serialize(PlayerInventory p, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			JsonObject armor = new JsonObject();
			armor.add("boots", getGson().toJsonTree(p.getBoots()));
			armor.add("chestplate", getGson().toJsonTree(p.getChestplate()));
			armor.add("helmet", getGson().toJsonTree(p.getHelmet()));
			armor.add("leggings", getGson().toJsonTree(p.getLeggings()));

			o.add("armor", armor);
			o.add("hand", getGson().toJsonTree(p.getItemInHand()));
			o.add("inventory", getGson().toJsonTree(Arrays.asList(p.getContents())));

			return o;
		}
	}

	public static class InventorySerializer implements JsonSerializer<Inventory> {
		@Override
		public JsonElement serialize(Inventory p, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(Arrays.asList(p.getContents()));
		}
	}

	public static class LocationSerializer implements JsonSerializer<Location> {
		@Override
		public JsonElement serialize(Location l, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("x", l.getX());
			o.addProperty("y", l.getY());
			o.addProperty("z", l.getZ());
			o.addProperty("pitch", l.getPitch());
			o.addProperty("yaw", l.getYaw());

			return o;
		}
	}

	public static class PluginArraySerializer implements JsonSerializer<Plugin[]> {
		@Override
		public JsonElement serialize(Plugin[] obj, Type type, JsonSerializationContext context) {
			JSONAPI.dbug("using custom Plugin[] serializer");
			List<Plugin> l = Arrays.asList((Plugin[]) obj);

			Collections.sort(l, new PluginSorter());

			return getGson().toJsonTree(l);
		}
	}

	public static class JSONAPIUserSerializer implements JsonSerializer<JSONAPIUser> {
		@Override
		public JsonElement serialize(JSONAPIUser u, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("username", u.getUsername());
			o.addProperty("password", u.getPassword());
			o.add("groups", getGson().toJsonTree(u.getGroups()));

			return o;
		}
	}

	public static class JSONAPIGroupSerializer implements JsonSerializer<JSONAPIGroup> {
		@Override
		public JsonElement serialize(JSONAPIGroup g, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("name", g.getName());
			o.add("streams", getGson().toJsonTree(g.getStreams()));
			o.add("methods", getGson().toJsonTree(g.getMethods()));
			o.add("permissions", getGson().toJsonTree(g.getPermissions()));

			return o;
		}
	}

	public static class JSONAPIPermissionNodeSerializer implements JsonSerializer<JSONAPIPermissionNode> {
		@Override
		public JsonElement serialize(JSONAPIPermissionNode p, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(p.getName());
		}
	}

	public static class GameModeSerializer implements JsonSerializer<GameMode> {
		@Override
		public JsonElement serialize(GameMode m, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(m.getValue());
		}
	}

	public static class EnchantmentSerializer implements JsonSerializer<Enchantment> {
		@Override
		public JsonElement serialize(Enchantment e, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(e.getId());
		}
	}

	public static class EconomyResponseSerializer implements JsonSerializer<EconomyResponse> {
		@Override
		public JsonElement serialize(EconomyResponse r, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.addProperty("amount", r.amount);
			o.addProperty("balance", r.balance);
			o.addProperty("errorMessage", r.errorMessage);
			o.addProperty("type", r.type.toString());

			return o;
		}
	}

	public static class AdminiumPushNotificationSerializer implements JsonSerializer<AdminiumPushNotification> {
		@Override
		public JsonElement serialize(AdminiumPushNotification not, Type type, JsonSerializationContext context) {
			JsonObject o = new JsonObject();

			o.add("date", getGson().toJsonTree(not.getDateSent()));
			o.addProperty("message", not.getMessage());

			return o;
		}
	}

	public static class DateSerializer implements JsonSerializer<Date> {
		static TimeZone tz = TimeZone.getTimeZone("UTC");
		static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		static {
			dateFormat.setTimeZone(tz);
		}

		@Override
		public JsonElement serialize(Date d, Type type, JsonSerializationContext context) {
			return getGson().toJsonTree(dateFormat.format(d));
		}
	}

}
