package com.alecgorge.minecraft.jsonapi.util;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.ChatterManager;
import com.dthielke.herochat.Herochat;
import com.dthielke.herochat.StandardChatter;

public class HerochatFauxPlayerInjector {
	private static boolean hasCheckedForHeroChat = false;
	private static boolean hasHerochat = false;
	
	@SuppressWarnings("unchecked")
	public static void inject(Player player) {
		if(!hasCheckedForHeroChat) {
			hasHerochat = Bukkit.getPluginManager().getPlugin("Herochat") != null;
			hasCheckedForHeroChat = true;
		}
		
		if (hasHerochat) {
			try {
				String name = player.getName();
				ChatterManager chatter_manager = Herochat.getChatterManager();
				Class<? extends ChatterManager> c = chatter_manager.getClass();
				Field f = c.getDeclaredField("chatters");
				f.setAccessible(true);

				Object o = f.get(chatter_manager);
				if (o instanceof Map) {
					Map<String, Chatter> m = (Map<String, Chatter>) o;
					if (!m.containsKey(name)) {
						StandardChatter chatter = new StandardChatter(
								chatter_manager.getStorage(), player);
						chatter.setActiveChannel(Herochat.getChannelManager()
								.getDefaultChannel(), false, true);
						Herochat.getChannelManager().getDefaultChannel()
								.addMember(chatter, false, true);

						m.put(name, chatter);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
