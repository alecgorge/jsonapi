package com.alecgorge.minecraft.jsonapi.chat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.util.OfflinePlayerLoader;

//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.entity.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.util.*;
//#else
import net.minecraft.server.v1_9_R1.*;

import org.bukkit.craftbukkit.v1_9_R1.*;
import org.bukkit.craftbukkit.v1_9_R1.entity.*;
import org.bukkit.craftbukkit.v1_9_R1.util.*;
//#endif

@SuppressWarnings("deprecation")
public class BukkitRealisticChat implements IRealisticChat {
	private Server	Server	= JSONAPI.instance.getServer();

	public Server getServer() {
		return Server;
	}
	
	public class FauxPlayer extends CraftPlayer {
		String name = null;
		UUID fakeUUID = null;
		
		public FauxPlayer(String fakeName, UUID fakeUUID, CraftPlayer player) {
			super((CraftServer)player.getServer(), player.getHandle());
			name = fakeName;
			this.fakeUUID = fakeUUID;
		}
		
		@Override
		public String getName() {
			if(name == null) {
				return super.getName();
			}
			return name;
		}
		
		@Override
		public String getDisplayName() {
			if(name == null) {
				return super.getDisplayName();
			}
			return name;
		}
		
		@Override
		public UUID getUniqueId() {
			if(fakeUUID == null) {
				return super.getUniqueId();
			}
			return super.getUniqueId();
		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @return damage taken since the last no damage ticks time period
		 */
		@Override
		public int _INVALID_getLastDamage() {
			return 0;
		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @param damage amount of damage
		 */
		@Override
		public void _INVALID_setLastDamage(int damage) {

		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @param amount Amount of damage to deal
		 */
		@Override
		public void _INVALID_damage(int amount) {

		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @param amount Amount of damage to deal
		 * @param source Entity which to attribute this damage from
		 */
		@Override
		public void _INVALID_damage(int amount, Entity source) {

		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @return Health represented from 0 to max
		 */
		@Override
		public int _INVALID_getHealth() {
			return 0;
		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @param health New health represented from 0 to max
		 * @throws IllegalArgumentException Thrown if the health is {@literal < 0 or >}
		 *                                  {@link #getMaxHealth()}
		 */
		@Override
		public void _INVALID_setHealth(int health) {

		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @return Maximum health
		 */
		@Override
		public int _INVALID_getMaxHealth() {
			return 0;
		}

		/**
		 * This method exists for legacy reasons to provide backwards
		 * compatibility. It will not exist at runtime and should not be used
		 * under any circumstances.
		 *
		 * @param health amount of health to set the maximum to
		 */
		@Override
		public void _INVALID_setMaxHealth(int health) {

		}
	}
	
	public boolean chatWithNameForPlayer(String message, String name, String playerToExtend) {
		Player player = getServer().getPlayerExact(name);
		
		// player isn't online
		if (player == null) {
			player = JSONAPI.loadOfflinePlayer(name);
		}
		
		// player doesn't exist. better fake something.
		if (player == null) {
			Player pe = null;			
			FauxPlayer f = null;
			if(getServer().getOfflinePlayers().length > 0) {
				pe = OfflinePlayerLoader.loadFromOfflinePlayer(getServer().getOfflinePlayers()[0]);					
			}
			else if(getServer().getOnlinePlayers().size() > 0) {
				pe = getServer().getOnlinePlayers().iterator().next();					
			}

			if(playerToExtend == null) {
				f = new FauxPlayer(name, UUID.randomUUID(), (CraftPlayer)pe);
			}
			else {
				f = new FauxPlayer(name, UUID.randomUUID(), (CraftPlayer)pe);
			}
			
			return chatWithPlayer(message, f);
		}

		return chatWithPlayer(message, player);
	}
	
	public boolean chatWithName(String message, String name) {
		return chatWithNameForPlayer(message, name, null);
	}

	public void pluginDisable() {

	}

	@Override
	public boolean canHandleChats() {
		return true;
	}

	public boolean chatWithPlayer(String message, Player player) {
		try {
			String s = message;
			boolean async = false;

			// based on
			// net/minecraft/server/v1_9_R1/PlayerConnection.java#chat(String, boolean)
			final MinecraftServer minecraftServer;

			if (getServer() instanceof CraftServer) {
				minecraftServer = ((CraftServer) getServer()).getServer();
			} else {
				System.err.println("Whoa, getServer() isn't a CraftServer?! I can't send a chat message now! It is a " + getServer().getClass().getCanonicalName());
				return false;
			}

			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(minecraftServer));
			getServer().getPluginManager().callEvent(event);

			Waitable<Void> waitable;
			if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
				final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
				queueEvent.setCancelled(event.isCancelled());
				waitable = new Waitable<Void>() {
					protected Void evaluate() {
						Bukkit.getPluginManager().callEvent(queueEvent);

						if (queueEvent.isCancelled()) {
							return null;
						}

						String message = String.format(queueEvent.getFormat(), new Object[] { queueEvent.getPlayer().getDisplayName(),queueEvent.getMessage() });

						minecraftServer.console.sendMessage(message);

						if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
							for (EntityPlayer player : (List<EntityPlayer>) minecraftServer.getPlayerList().players) {
								player.sendMessage(CraftChatMessage.fromString(message));
							}
						}
						else {
							for (Player player : queueEvent.getRecipients()) {
								player.sendMessage(message);
							}
						}

						return null;
					}
				};
				if (async)
					minecraftServer.processQueue.add(waitable);
				else
					waitable.run();
				try {
					waitable.get();
				}
				catch (InterruptedException localInterruptedException) {
					Thread.currentThread().interrupt();
				}
				catch (ExecutionException e) {
					throw new RuntimeException("Exception processing chat event", e.getCause());
				}
			}
			else {
				if (event.isCancelled()) {
					JSONAPI.dbug("Chat event cancelled");
					return false;
				}

				s = String.format(event.getFormat(), new Object[] { event.getPlayer().getDisplayName(),event.getMessage() });
				minecraftServer.console.sendMessage(s);
				if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
					for (EntityPlayer recipient : (List<EntityPlayer>) minecraftServer.getPlayerList().players) {
						recipient.sendMessage(CraftChatMessage.fromString(s));
					}
				}
				else {
					for (Player p : event.getRecipients()) {
						p.sendMessage(message);
					}
				}
			}

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}
}
