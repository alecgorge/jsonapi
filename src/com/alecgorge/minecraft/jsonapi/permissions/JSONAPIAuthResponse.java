package com.alecgorge.minecraft.jsonapi.permissions;

public class JSONAPIAuthResponse {
	private boolean authenticated;
	private boolean allowed;
	
	private String message = null;
	
	public JSONAPIAuthResponse(boolean al, boolean auth) {
		authenticated = auth;
		allowed = al;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public String getMessage() {
		if(message == null) {
			if(authenticated == false) {
				return "Invalid username, password or salt.";
			}
			if(allowed == false) {
				return "You had the correct username, password and salt but you just aren't allowed to use this API method.";
			}
		}
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
