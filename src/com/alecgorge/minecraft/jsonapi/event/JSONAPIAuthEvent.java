package com.alecgorge.minecraft.jsonapi.event;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class JSONAPIAuthEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	String method;
	String hash;
	HashMap<String, String> logins;
	boolean valid;

	public JSONAPIAuthEvent(boolean valid, String method, String hash, HashMap<String, String> logins) {
		this.valid = valid;
		this.method = method;
		this.hash = hash;
		this.logins = logins;
	}

	public String getMethod() {
		return method;
	}

	public String getHash() {
		return hash;
	}

	public HashMap<String, String> getLogins() {
		return logins;
	}

	public boolean getValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String calculateSHA256Hash(String input) {
		try {
			return JSONAPI.SHA256(input);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "abc";
		}
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
