package com.alecgorge.minecraft.jsonapi.chat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class BukkitRealisticChat implements IRealisticChat {
	private Server Server = JSONAPI.instance.getServer();

	public Server getServer() {
		return Server;
	}
	
	public boolean chatWithName(String message, String name) {
		try {
			Player player = getServer().getPlayerExact(name);
			if(player == null)
				player = JSONAPI.loadOfflinePlayer(name);

			String s = message;
			boolean async = false;

			// copied from CraftBukkit / src / main / java / net / minecraft /
			// server / NetServerHandler.java#chat(2)
			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new HashSet<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers())));
			getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				System.out.println("cancelled");
				return true;
			}

			s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
			boolean isLazy = false;
			if(!(event.getRecipients() instanceof HashSet)) {
				try {
					Method m = event.getRecipients().getClass().getMethod("isLazy");
					Boolean.valueOf(m.invoke(event.getRecipients()).toString());
				}
				catch (NoSuchMethodException e) {
					
				}
			}
			
			if (isLazy) {
				for (Player p : Bukkit.getServer().getOnlinePlayers())
					p.sendMessage(s);
			} else {
				for (Player recipient : event.getRecipients())
					recipient.sendMessage(s);
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void pluginDisable() {

	}

	@Override
	public boolean canHandleChats() {
		return true;
	}
}
