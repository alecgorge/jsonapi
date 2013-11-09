package com.alecgorge.minecraft.jsonapi.util;

import java.io.File;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.EntityPlayer;
//$import net.minecraft.server./*$mcversion$*/.MinecraftServer;
//$import net.minecraft.server./*$mcversion$*/.PlayerInteractManager;
//$import org.bukkit.craftbukkit./*$mcversion$*/.CraftServer;
//#else
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.MinecraftServer;
import net.minecraft.server.v1_6_R3.PlayerInteractManager;
import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
//#endif

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class OfflinePlayerLoader {
	public static Player load(String exactPlayerName) {
		// big thanks to
		// https://github.com/lishd/OpenInv/blob/master/src/com/lishid/openinv/internal/craftbukkit/PlayerDataManager.java
		// Offline inv here...
		try {
			// See if the player has data files

			// Find the player folder
			File playerfolder = new File(((World)Bukkit.getWorlds().get(0)).getWorldFolder(), "players");
			if (!playerfolder.exists()) {
				return null;
			}

			Player target = null;
			try {
				MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
				
				// Create an entity to load the player data
				EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), exactPlayerName, new PlayerInteractManager(server.getWorldServer(0)));
	
				// Get the bukkit entity
				target = (entity == null) ? null : entity.getBukkitEntity();
			} catch (Exception e) {
				MinecraftServer server = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
				
				// Create an entity to load the player data
				EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), exactPlayerName, new PlayerInteractManager(server.getWorldServer(0)));
	
				// Get the bukkit entity
				target = (entity == null) ? null : entity.getBukkitEntity();				
			}
			
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
		}

		return null;
	}
}
