package com.alecgorge.minecraft.jsonapi.chat;

import java.util.logging.Logger;

import org.bukkit.Server;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class BukkitForgeRealisticChat implements IRealisticChat {
	private Server Server = JSONAPI.instance.getServer();
	
	public Server getServer() {
		return Server;
	}
	
	public boolean chatWithName(String message, String name) {
		String line = new StringBuilder().append("<").append(name).append("> ").append(message).toString();
		Logger.getLogger("ForgeModLoader").info(line);
		Server.broadcastMessage(line);
		
		return true;
	}

	public void pluginDisable() {
	}

	@Override
	public boolean canHandleChats() {
		try {
			Class.forName("net.minecraftforge.event.ServerChatEvent");
			return true;
		}
		catch(Exception e) {
			return false;
		}
		catch(Error e) {
			return false;
		}
	}
}
