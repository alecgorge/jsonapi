package com.alecgorge.minecraft.jsonapi.chat;

import java.util.logging.Logger;

import org.bukkit.Server;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class BukkitForgeRealisticChat implements IRealisticChat {
	private Server Server = JSONAPI.instance.getServer();
	
	public Server getServer() {
		return Server;
	}

//	@SuppressWarnings("unchecked")
	public boolean chatWithName(String message, String name) {
		// BukkitForgeNetHandler.java#chat(String name, String s)
		String line = new StringBuilder().append("<").append(name).append("> ").append(message).toString();
		Logger.getLogger("ForgeModLoader").info(line);
		Server.broadcastMessage(line);

		return true;

//		try {
//			Class<?> rJc = Class.forName("jc");
//			Constructor<?> rJcEventCon = rJc.getConstructors()[0];
//
//			CraftServer cs = (CraftServer) Server;
//			MinecraftServer minecraftServer = cs.getHandle();
//			CraftWorld cw = (CraftWorld) cs.getWorlds().get(0);
//
//			Class<?> rJd = Class.forName("jd");
//			Constructor<?> rJdCon = rJd.getConstructors()[0];
//			Object jd = rJdCon.newInstance(cw.getHandle());
//
//			Object jc = rJcEventCon.newInstance(cs.getHandle(), cw.getHandle(), name, jd);
//
//			Class<?> toBukkit = ToBukkit.class;
//			Method makePlayer = toBukkit.getDeclaredMethod("player", Class.forName("sq"));
//
//			CraftPlayer player = (CraftPlayer) makePlayer.invoke(null, jc);
//
//			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, message, new LazyPlayerSet());
//			Server.getPluginManager().callEvent(event);
//
//			Class<?> rForgeEvent = ServerChatEvent.class;
//			Constructor<?> forgeEventConst = rForgeEvent.getConstructors()[0];
//
//			ServerChatEvent forgeEvent = (ServerChatEvent) forgeEventConst.newInstance(jc, message, new StringBuilder().append("<").append(name).append("> ").append(message).toString());
//			if (MinecraftForge.EVENT_BUS.post(forgeEvent)) {
//				return true;
//			}
//
//			if (event.isCancelled()) {
//				return true;
//			}
//
//			String s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
//
//			if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
//				for (Object o : minecraftServer.ad().a) {
//					Method m = o.getClass().getMethod("a", String.class);
//
//					m.invoke(o, s);
//				}
//			} else {
//				for (Player recipient : event.getRecipients()) {
//					recipient.sendMessage(s);
//				}
//			}
//		} catch (Exception e) {
//			Server.getLogger().severe("Error chatting!");
//			e.printStackTrace();
//
//			return false;
//		}
//
//		return true;
	}

	public void pluginDisable() {
	}

	@Override
	public boolean canHandleChats() {
		return true;
	}
}
