package com.alecgorge.minecraft.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class PermissionWrapper {
	Server server;
	boolean active = false;

	Permission perms;

	public PermissionWrapper(Server s) {
		if (s.getPluginManager().getPlugin("Vault") == null) {
			Logger.getLogger("Minecraft").info("[JSONAPI] You don't have Vault installed, you cannot use permission methods!");
		} else {
			active = true;
			server = s;
			RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>)server.getServicesManager().getRegistration(Permission.class);
			perms = rsp.getProvider();
		}
	}

	private Player getPlayerExact(String playerName) {
		Player player = server.getPlayerExact(playerName);
		if (player == null) {
			player = JSONAPI.loadOfflinePlayer(playerName);
		}

		return player;
	}

	public List<String> getGroups(String playerName) {
		if (active) {
			try {
				return Arrays.asList(perms.getPlayerGroups(getPlayerExact(playerName)));
			} catch (Exception e) {
				return new ArrayList<String>();
			}
		}

		return new ArrayList<String>();
	}
	
	public List<String> getPlayersInGroup(String group) {
		List<String> players = new ArrayList<String>();
		if(active) {
			for (Player p : server.getOnlinePlayers()) {
				if(Arrays.asList(perms.getPlayerGroups(p)).indexOf(group) > -1) {
					players.add(p.getName());
				}
			}
			for (OfflinePlayer p : server.getOfflinePlayers()) {
				Player pp = JSONAPI.loadOfflinePlayer(p.getName());
				if(Arrays.asList(perms.getPlayerGroups(pp)).indexOf(group) > -1) {
					players.add(pp.getName());
				}
			}
		}
		return players;
	}

	public List<String> getAllGroups() {
		if (active) {
			return Arrays.asList(perms.getGroups());
		}

		return new ArrayList<String>();
	}

	public boolean addGroup(String player, String group) {
		Player p = getPlayerExact(player);
		boolean r = active ? perms.playerAddGroup(p, group) : false;
		p.saveData();
		return r;
	}

	public boolean removeGroup(String player, String group) {
		Player p = getPlayerExact(player);
		boolean r = active ? perms.playerRemoveGroup(p, group) : false;
		p.saveData();
		return r;
	}

	public boolean addPermission(String playername, String key, Boolean value) {
		try {
			Player p = getPlayerExact(playername);
			p.addAttachment(JSONAPI.instance, key, value);
			p.saveData();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean removePermission(String playername, String key) {
		try {
			Player player = server.getPlayerExact(playername);
			Set<PermissionAttachmentInfo> eps = player.getEffectivePermissions();

			PermissionAttachment a = null;
			for (PermissionAttachmentInfo o : eps) {
				if (o.getPermission().equals(key)) {
					a = o.getAttachment();
					break;
				}
			}
			if (a != null) {
				player.removeAttachment(a);
				player.recalculatePermissions();
			}

			player.saveData();

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<JSONObject> getPermissions(String playername) {
		List<JSONObject> perms = new ArrayList<JSONObject>();
		try {
			Player player = server.getPlayerExact(playername);
			Set<PermissionAttachmentInfo> eps = player.getEffectivePermissions();

			for (PermissionAttachmentInfo o : eps) {
				JSONObject oo = new JSONObject();

				oo.put("key", o.getPermission());
				oo.put("value", o.getValue());

				perms.add(oo);
			}

			return perms;
		} catch (Exception e) {
			if(JSONAPI.shouldDebug) e.printStackTrace();
			return perms;
		}
	}

	public Map<String, List<JSONObject>> getAllPermissions() {
		Map<String, List<JSONObject>> l = new HashMap<String, List<JSONObject>>();
		try {
			for (Player p : server.getOnlinePlayers()) {
				l.put(p.getName(), getPermissions(p.getName()));
			}
		} catch (Exception e) {
		}
		return l;
	}
}
