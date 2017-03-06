package com.alecgorge.minecraft.jsonapi.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.EntityPlayer;
//$import net.minecraft.server./*$mcversion$*/.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.*;
//#else
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.*;
//#endif

public class OfflinePlayerLoader {
	public static Player load(String exactPlayerName) {
		// big thanks to
		// https://github.com/lishid/OpenInv/blob/master/src/main/java/com/lishid/openinv/internal/v1_8_R1/PlayerDataManager.java
		// Offline inv here...

		try {
			UUID uuid = matchUser(exactPlayerName);
			if (uuid == null) {
				return null;
			}

			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			return loadFromOfflinePlayer(player);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		catch (Error e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static Player loadFromOfflinePlayer(OfflinePlayer player) {
		if (player == null) {
			return null;
		}
		
		GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		// Create an entity to load the player data
		EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));

		// Get the bukkit entity
		Player target = entity.getBukkitEntity();
		if (target != null) {
			// Load data
			target.loadData();
			// Return the entity
			return target;
		}
		
		return null;
	}

	public static UUID matchUser(String search) {
		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
		for (OfflinePlayer player : offlinePlayers) {
			String name = player.getName();

			if (name == null) {
				continue;
			}
			if (name.equalsIgnoreCase(search)) {
				return player.getUniqueId();
			}
		}

		return null;
	}
}
