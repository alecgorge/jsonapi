package com.alecgorge.minecraft.jsonapi.api.v2;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.alecgorge.minecraft.jsonapi.APIException;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.dynamic.API_Method;
import com.alecgorge.minecraft.jsonapi.dynamic.JSONAPIMethodProvider;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class EssentialsAPIMethods implements JSONAPIMethodProvider {
	private Essentials	ess;
	
	public EssentialsAPIMethods(JSONAPI api) {
		if (api.getServer().getPluginManager().getPlugin("Essentials") != null) {
			api.getCaller().registerMethods(this);
		}
	}

	@API_Method(name = "essentials.reload", description = "Reloads the plugin", argumentDescriptions = { "void" }, returnDescription = "void")
	public void reload() {
		ess.reload();
	}

	@API_Method(name = "essentials.config.get_keys", argumentDescriptions = { "void" }, returnDescription = "Gets the list of keys as Set<string>")
	public Set<String> get_configuration() {
		FileConfiguration x = ess.getConfig();
		return x.getKeys(true);
	}

	@API_Method(name = "essentials.config.get_text", argumentDescriptions = { "void" }, returnDescription = "Gets the whole configuration file as String")
	public String get_configuration_text() {
		FileConfiguration x = ess.getConfig();
		return x.saveToString();
	}

	@API_Method(name = "essentials.config.get_value", argumentDescriptions = { "Path to the key" }, returnDescription = "path as key.key.key...")
	public Object get_configuration_value(String path) {
		FileConfiguration x = ess.getConfig();
		return x.get(path);
	}


	@API_Method(name = "essentials.config.set_value", argumentDescriptions = { "Path to config key", "The value to set", "Should the plugin be reloaded after changing the value" }, returnDescription = "path as key.key.key...")
	public boolean set_configuration_value(String path, Object value, boolean should_reload) throws IOException {
		FileConfiguration x = ess.getConfig();
		x.set(path, value);
		File configFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Essentials").getDataFolder(), "config.yml");
		x.save(configFile);
		if (should_reload) {
			reload();
		}
		return true;
	}


	@API_Method(name = "essentials.config.restore_default", description = "Resets the configuration file to it's default", argumentDescriptions = { "Should the plugin be reloaded after the operation" }, returnDescription = "true on success")
	public boolean restore_default(boolean should_reload) {
		Configuration conf = ess.getConfig().getDefaults();
		ess.getConfig().setDefaults(conf);
		if (should_reload) {
			reload();
		}
		return true;
	}


	@API_Method(name = "essentials.users.get_uuid", description = "Returns the UUID of a player(has to be online)", argumentDescriptions = { "Username" }, returnDescription = "on success")
	public Object get_uuid(String name) throws APIException {
		if (ess.getUser(name).getBase().isOnline()) {
			Object x = ess.getUser(name).getBase().getUniqueId();
			return x;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.add_mail", description = "Adds a mail to users' mailbox", argumentDescriptions = { "Username", "Contents of the message" }, returnDescription = "true on success")
	public boolean send_mail(String name, String message) {
		ess.getUser(name).addMail(message);
		return true;
	}


	@API_Method(name = "essentials.users.add_mail_all", description = "Adds a mail to all users", argumentDescriptions = { "Contents of the message" }, returnDescription = "true on success")
	public boolean send_mail_all(String message) {
		for (UUID userUuid : ess.getUserMap().getAllUniqueUsers()) {
			User user = ess.getUserMap().getUser(userUuid);
			if (user != null) {
				user.addMail(message);
			}
		}
		return true;
	}


	@API_Method(name = "essentials.users.set_afk", description = "Change status of AFK of user", argumentDescriptions = { "Status to set (true - AFK)" }, returnDescription = "true on success")
	public boolean set_afk(String name, Boolean status) throws APIException {
		if (ess.getUser(name).getBase().isOnline()) {
			ess.getUser(name).setAfk(status);
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.get_base", description = "Change status of AFK of user", argumentDescriptions = { "Status to set" }, returnDescription = "true on success")
	public Object get_base(String name) throws APIException {
		return ess.getUser(name).getBase().getPlayer();
	}


	@API_Method(name = "essentials.users.tp_back", description = "Teleport player to last location", argumentDescriptions = { "Username" }, returnDescription = "true on success")
	public boolean tp_back(String name) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			if (user.getLastLocation() == null) {
				throw new APIException("noLocationFound");
			}
			else {
				try {
					user.getTeleport().back();
					return true;
				}
				catch (Exception ex) {
					throw new APIException(ex.toString());
				}
			}
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.clear_inventory", description = "Clear inventory and armor of player", argumentDescriptions = { "Username", "Clear armor?" }, returnDescription = "true on success")
	public boolean clear_inventory(String name, Boolean armor) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.getBase().getInventory().clear();
			if (armor) {
				user.getBase().getInventory().setArmorContents(null);
			}
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.burn", description = "Set the player on fire.", argumentDescriptions = { "Username", "Time" }, returnDescription = "true on success")
	public boolean burn(String name, Integer time) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.getBase().setFireTicks(time);
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.fly", description = "Change fly mode of player.", argumentDescriptions = { "Username", "Mode (true - flying)" }, returnDescription = "true on success")
	public boolean fly(String name, Boolean mode) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.getBase().setFlying(mode);
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.get_pos", description = "Return location of player", argumentDescriptions = { "Username" }, returnDescription = "Location of player on success")
	public Object get_pos(String name) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			return user.getLocation();
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.toggle_god", description = "Change god mode of player", argumentDescriptions = { "Username", "Mode" }, returnDescription = "true on success")
	public Boolean toggle_god(String name, Boolean mode) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.setGodModeEnabled(mode);
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.heal", description = "Heal the player", argumentDescriptions = { "Username" }, returnDescription = "true on success")
	public Boolean heal(String name) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.getBase().setHealth(20);
			user.getBase().setFoodLevel(20);
			user.getBase().setFireTicks(0);
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.set_nick", description = "Change the nickanme of player", argumentDescriptions = { "Username", "Nickname to set" }, returnDescription = "true on success")
	public Boolean set_nick(String name, String nickname) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getDisplayName().toLowerCase().equals(nickname.toLowerCase())) {
					throw new APIException("Nickname is used");
				}
			}
			user.setNickname(nickname);
			user.setDisplayNick();
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.clear_nick", description = "Clear the nickanme of player", argumentDescriptions = { "Username" }, returnDescription = "true on success")
	public Boolean clear_nick(String name) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.setNickname(null);
			user.setDisplayNick();
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.clear_all_powertools", description = "Clear all powertools of player", argumentDescriptions = { "Username" }, returnDescription = "true on success")
	public Boolean clear_all_powertools(String name) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.clearAllPowertools();
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.seen", description = "Return the last logout time of player.", argumentDescriptions = { "Username" }, returnDescription = "Timestamp if offline, -1 if online")
	public Object seen(String name) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			return -1;
		}
		else {
			return user.getLastLogout();
		}
	}


	@API_Method(name = "essentials.users.vanish", description = "Change visible of player", argumentDescriptions = { "Username", "Mode to set" }, returnDescription = "true on success")
	public Boolean vanish(String name, Boolean mode) throws APIException {
		User user = ess.getUser(name);
		if (user.getBase().isOnline()) {
			user.setVanished(mode);
			return true;
		}
		else {
			throw new APIException("User is not online");
		}
	}


	@API_Method(name = "essentials.users.player_get_all", description = "Return all informations about player", argumentDescriptions = { "Username" }, returnDescription = "on success")
	public HashMap<String, Object> player_get_all(String name) throws APIException {
		HashMap<String, Object> result = new HashMap<String, Object>();
		User user1 = ess.getUser(name);
		if (user1.getBase().hasPlayedBefore()) {
			if (user1.getBase().isOnline()) {
				User user = ess.getUser(name);
				result.put("name", user.getName());
				result.put("nick", user.getNickname());
				result.put("money", user.getMoney());
				result.put("gamemode", user.getBase().getGameMode());
				result.put("godmode", user.isGodModeEnabled());
				result.put("fly", user.isGodModeEnabled());
				result.put("afk", user.isAfk());
				result.put("jail", user.isJailed());
				if (user.isJailed()) {
					result.put("jail_timeout", user.getJailTimeout());
				}
				else {
					result.put("jail_timeout", null);
				}
				result.put("mute", user.isMuted());
				if (user.isMuted()) {
					result.put("mute_timeout", user.getMuteTimeout());
				}
				else {
					result.put("mute_timeout", null);
				}
				result.put("can_build", user.canBuild());
				result.put("vanish", user.isVanished());
				result.put("bed_spawn_location", user.getBase().getBedSpawnLocation());
				result.put("ban", user.getBase().isBanned());
				if (user.getBase().isBanned()) {
					result.put("ban_reason", Bukkit.getBanList(BanList.Type.NAME).getBanEntry(user.getName()).getReason().replaceAll("�", ""));
					result.put("ban_timeout", Bukkit.getBanList(BanList.Type.NAME).getBanEntry(user.getName()).getExpiration());
				}
				else {
					result.put("ban_reason", null);
					result.put("ban_timeout", null);
				}
				result.put("can_pickup_items", user.getBase().getCanPickupItems());
				result.put("fire_ticks", user.getBase().getFireTicks());
				result.put("homes", user.getHomes());
				result.put("mails", user.getMails());
				result.put("walk_speed", user.getBase().getWalkSpeed());
			}
			else {
				User user = ess.getOfflineUser(name);
				result.put("name", user.getName());
				result.put("last_ip", user.getLastLoginAddress());
				result.put("nick", user.getNickname());
				result.put("money", user.getMoney());
				result.put("bed_spawn_location", user.getBase().getBedSpawnLocation());
				result.put("ban", user.getBase().isBanned());
				if (user.getBase().isBanned()) {
					result.put("ban_reason", Bukkit.getBanList(BanList.Type.NAME).getBanEntry(user.getName()).getReason().replaceAll("�", ""));
					result.put("ban_timeout", Bukkit.getBanList(BanList.Type.NAME).getBanEntry(user.getName()).getExpiration());
				}
				else {
					result.put("ban_reason", null);
					result.put("ban_timeout", null);
				}
				result.put("homes", user.getHomes());
				result.put("mails", user.getMails());
			}
			return result;
		}
		else {
			throw new APIException("Player has never been on the server.");
		}
	}
}
