package com.alecgorge.minecraft.jsonapi.api;

import java.util.Calendar;

import org.json.simpleForBukkit.JSONAware;
import org.json.simpleForBukkit.JSONObject;

/**
 * This class will need to be subcalled to provide meaningful data. By default, there are just some convenience methods.
 * 
 * It is recommended that you call "setTime()" in your constructor to make sure the timestamp is as accurate as possible.
 * 
 * @author alecgorge
 */
abstract public class JSONAPIStreamMessage implements JSONAware {
	protected long time = -1;

	/**
	 * This return value of this method should be the same as the name of the stream when register it with JSONAPI.
	 * 
	 * @return the stream name
	 */
	public abstract String streamName();
	
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * If setTime was previously called, returns the value setTime set. Otherwise, calls setTime and returns the value. 
	 * 
	 * @return The timestamp store.
	 */
	public long getTime() {
		if(time == -1) {
			setTime();
		}
		return time;
	}

	/**
	 * Takes a timestamp of the current time and stores it for usage later when the JSONObject is being created.
	 */
	public void setTime() {
		Calendar cal = Calendar.getInstance();
		long lUTCtime = cal.getTimeInMillis();

		time = lUTCtime/1000;
	}

	/**
	 * Returns an org.json.simpleForBukkit.JSONObject that is a JSON representation of the data in this class.
	 * 
	 * Example:
	 * 
	 * <code>
	 * JSONObject o = new JSONObject();
	 * o.put("time", getTime());
	 * o.put("player", getPlayer());
	 * o.put("message", getMessage());
	 * return o;
	 * </code>
	 * @return
	 */
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
