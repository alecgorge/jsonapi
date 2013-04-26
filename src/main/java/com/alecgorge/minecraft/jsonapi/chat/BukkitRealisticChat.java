package com.alecgorge.minecraft.jsonapi.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import net.minecraft.server.v1_5_R2.Connection;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.MinecraftServer;
import net.minecraft.server.v1_5_R2.NetworkManager;
import net.minecraft.server.v1_5_R2.PlayerConnection;
import net.minecraft.server.v1_5_R2.PlayerInteractManager;
import net.minecraft.server.v1_5_R2.World;

import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R2.util.LazyPlayerSet;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class BukkitRealisticChat implements IRealisticChat {
	private Server Server = JSONAPI.instance.getServer();
	
	public Server getServer() {
		return Server;
	}
	
	class FauxPlayer extends CraftPlayer {
		String name;

		public FauxPlayer(String name, FauxEntityPlayer ent) {
			super((CraftServer) JSONAPI.instance.getServer(), ent);

			this.name = name;

			((FauxPlayerConnection) getHandle().playerConnection).setPlayer(this);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isOnline() {
			return true;
		}

		@Override
		public boolean isOp() {
			return true;
		}

		@Override
		public void sendMessage(String message) {
			if(message.isEmpty()) return;

			JSONAPI.instance.getLogger().info("[FauxPlayer] " + message);

			// JSONAPI.instance.jsonServer.logChat("", message);
		}
	}

	class FauxEntityPlayer extends EntityPlayer {

		public FauxEntityPlayer(MinecraftServer minecraftserver, World world, String s, PlayerInteractManager iteminworldmanager) {
			super(minecraftserver, world, s, iteminworldmanager);

			Socket ss = null;
			try {
				ss = new Socket("localhost", fauxPort);
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
			NetworkManager m = null;
			try {
				m = new NetworkManager(null, ss, "???", new Connection() {

					@Override
					public boolean a() {
						// TODO Auto-generated method stub
						return false;
					}
				}, null);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			playerConnection = new FauxPlayerConnection(((CraftServer) getServer()).getServer(), m, this);

			try {
				ss.close();
			} catch (IOException e) {
			}
		}

	}

	class FauxPlayerConnection extends PlayerConnection {
		private CraftPlayer _player;

		public FauxPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
			super(minecraftserver, networkmanager, entityplayer);
		}

		public void setPlayer(CraftPlayer p) {
			_player = p;
		}

		public CraftPlayer getPlayerExact() {
			return _player;
		}
	}

	private HashMap<String, FauxPlayer> joinedList = new HashMap<String, FauxPlayer>();
	private ServerSocket fauxServer = null;
	private int fauxPort = 0;
	private Plugin herochat = null;

	public boolean chatWithName(String message, String name) {
		if (fauxServer == null) {
			try {
				fauxServer = new ServerSocket(0);
				fauxPort = fauxServer.getLocalPort();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(herochat == null) {
			Plugin p = getServer().getPluginManager().getPlugin("Herochat");
			if(p != null) {
				herochat = p;
			}
		}
		
		try {
			FauxPlayer player;
			if (joinedList.containsKey(name)) {
				player = joinedList.get(name);
			} else {
				// this is the biggest hack ever.
				player = new FauxPlayer(name, new FauxEntityPlayer(((CraftServer) Server).getServer(), ((CraftWorld) Server.getWorlds().get(0)).getHandle(), name, new PlayerInteractManager(((CraftServer) Server).getServer().getWorldServer(0))));
				joinedList.put(name, player);

//				if(Server.getPlayerExact(name) == null) {
//					PlayerJoinEvent joinE = new PlayerJoinEvent(player, "jsonapi fauxplayer join");
//					Server.getPluginManager().callEvent(joinE);
//				}

				if(herochat != null) {
					HerochatFauxPlayerInjector.inject(player);
				}
			}

			final MinecraftServer minecraftServer = MinecraftServer.getServer();
			String s = message;
			boolean async = false;
			
			// copied from CraftBukkit / src / main / java / net / minecraft /
			// server / NetServerHandler.java#chat(2)
			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet());
			Server.getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return true;
			}

			s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
			// minecraftServer.console.sendMessage(s);
			if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
				for (Object recipient : minecraftServer.getPlayerList().players) {
					((EntityPlayer) recipient).sendMessage(s);
				}
			} else {
				for (Player recipient : event.getRecipients()) {
					recipient.sendMessage(s);
				}
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
		try {
			return PlayerInteractManager.class != null;
		} catch (Exception e) {
			return false;
		} catch (Error e) {
			return false;
		}
	}
}
