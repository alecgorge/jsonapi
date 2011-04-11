package com.ramblingwood.minecraft.jsonapi.streams;

import java.util.Calendar;

import org.json.simpleForBukkit.JSONAware;
import org.json.simpleForBukkit.JSONObject;

public abstract class JSONAPIStream implements JSONAware {
	protected String player;
	protected String message;
	protected long time;
	
	
	public JSONAPIStream(String player, String message) {
		this.player = player;
		this.message = message;
	}
	
	public String getMessage () {
		return message;
	}
	
	public String getPlayer () {
		return player;
	}
	
	public long getTime () {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public void setTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, -cal.get(Calendar.DST_OFFSET) - cal.get(Calendar.ZONE_OFFSET));
		long lUTCtime = cal.getTimeInMillis();
		
		time = lUTCtime/1000;
	}
	
	public String toJSONString() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", getPlayer());
		o.put("message", getMessage());
		
		return o.toJSONString();
	}	
}
