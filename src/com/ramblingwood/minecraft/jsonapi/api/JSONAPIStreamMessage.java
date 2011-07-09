package com.ramblingwood.minecraft.jsonapi.api;

import java.util.Calendar;

import org.json.simpleForBukkit.JSONAware;
import org.json.simpleForBukkit.JSONObject;

abstract public class JSONAPIStreamMessage implements JSONAware {
	protected long time = -1;

	public abstract String streamName();
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTime() {
		if(time == -1) {
			setTime();
		}
		return time;
	}

	public void setTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, -cal.get(Calendar.DST_OFFSET) - cal.get(Calendar.ZONE_OFFSET));
		long lUTCtime = cal.getTimeInMillis();

		time = lUTCtime/1000;
	}

	public abstract JSONObject toJSONObject ();
	/*
		Example:
		
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", getPlayer());
		o.put("message", getMessage());
		return o;
	*/

	public String toJSONString() {
		return toJSONObject().toJSONString();
	}	
}
