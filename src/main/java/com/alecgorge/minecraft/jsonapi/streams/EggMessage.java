package com.alecgorge.minecraft.jsonapi.streams;

import org.bukkit.event.player.PlayerEggThrowEvent;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class EggMessage extends JSONAPIStreamMessage {
	public String player;
        public double eggX;
        public double eggZ;
        public double eggY;
        public String eggWorld;

	public EggMessage(PlayerEggThrowEvent e) {
		this.player = e.getPlayer().getName();
		this.eggWorld = e.getEgg().getLocation().getWorld().getName();
		this.eggX = e.getEgg().getLocation().getX();
		this.eggY = e.getEgg().getLocation().getY();
		this.eggZ = e.getEgg().getLocation().getZ();
		setTime();
	}
	
	public String streamName () {
		return "egg";
	}

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", player);
		o.put("egg_x", eggX);
		o.put("egg_y", eggY);
		o.put("egg_z", eggZ);
		o.put("egg_world", eggWorld);
		return o;
	}
}
