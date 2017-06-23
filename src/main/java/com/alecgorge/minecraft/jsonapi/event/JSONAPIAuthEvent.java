package com.alecgorge.minecraft.jsonapi.event;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIAuthResponse;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;

public class JSONAPIAuthEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	String method;
	String hash;
	HashMap<String, String> logins;
	JSONAPIAuthResponse valid;
	String username;
	boolean stream;

	public JSONAPIAuthEvent(JSONAPIAuthResponse valid, String method, String hash, HashMap<String, String> logins, String username, boolean stream) {
		this.valid = valid;
		this.method = method;
		this.hash = hash;
		this.logins = logins;
		this.username = username;
		this.stream = stream;
	}

	public JSONAPIAuthEvent(JSONAPIAuthResponse valid, String method, String username, boolean stream) {
		this.valid = valid;
		this.method = method;
		this.username = username;
		this.stream = stream;
	}
	
	public boolean isStream() {
		return stream;
	}

	public String getUsername() {
		return username;
	}
	
	public JSONAPIUser getUser() {
		return JSONAPI.instance.getAuthTable().getUser(getUsername());
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

	public JSONAPIAuthResponse getAuthResponse() {
		return valid;
	}

	public String calculateSHA256Hash(String input) {
		try {
			return JSONAPI.SHA256(input);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "abbBESTHASHEVER";
		}
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
