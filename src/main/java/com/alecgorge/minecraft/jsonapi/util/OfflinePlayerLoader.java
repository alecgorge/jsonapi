package com.alecgorge.minecraft.jsonapi.util;

import java.io.File;
import java.util.UUID;
import java.lang.reflect.Method;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.*;
//#if mc17OrNewer=="yes"
import net.minecraft.util.com.mojang.authlib.GameProfile;
//#endif
//#else
import net.minecraft.server.v1_7_R1.*;
import org.bukkit.craftbukkit.v1_7_R1.*;
//#endif

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class OfflinePlayerLoader {
	public static Player load(String exactPlayerName) {
		// big thanks to
		// https://github.com/lishd/OpenInv/blob/master/src/com/lishid/openinv/internal/craftbukkit/PlayerDataManager.java
		// Offline inv here...
		
		OfflinePlayer player = Bukkit.getOfflinePlayer(exactPlayerName);
		
		int index = 0;
		for (World w : Bukkit.getWorlds()) {
			try {
				// See if the player has data files

				// Find the player folder
				File playerfolder = new File(w.getWorldFolder(), "players");
				if (!playerfolder.exists()) {
					playerfolder = new File(w.getWorldFolder(), "playerdata");
				}
				
				if(!playerfolder.exists()) {
					continue;
				}
				
				Player target = null;
				MinecraftServer server = null;
				try {
					server = ((CraftServer) Bukkit.getServer()).getServer();
				} catch (Exception e) {
					server = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
				}

				// Create an entity to load the player data
				//#if mc179OrNewer=="yes"
				//$UUID id = null;
				//$
				//$if(player == null) {
				//$	id = UUID.randomUUID();
				//$}
				//$else {
				//$	id = player.getUniqueId();
				//$}
				//$
				//$EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(index), new GameProfile(id, exactPlayerName), new PlayerInteractManager(server.getWorldServer(index)));
				//#else
					//#if mc17OrNewer=="yes"				
					EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(index), new GameProfile(null, exactPlayerName), new PlayerInteractManager(server.getWorldServer(index)));				
					//#else
					//$EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(index), exactPlayerName, new PlayerInteractManager(server.getWorldServer(index)));
					//#endif
				//#endif

				// Get the bukkit entity
				target = (entity == null) ? null : entity.getBukkitEntity();

				if (target != null) {
					// Load data
					target.loadData();
					// Return the entity
					return target;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			} finally {
				index++;
			}
		}

		return null;
	}
}
