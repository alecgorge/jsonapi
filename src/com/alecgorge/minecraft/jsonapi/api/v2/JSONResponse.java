package com.alecgorge.minecraft.jsonapi.api.v2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.APIException;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIAuthResponse;

public class JSONResponse {
	String tag = "";
	String methodName;
	String username;
	String key;
	JSONArray arguments;
	boolean stream = false;
	boolean showOlder = false;
	
	NanoHTTPD httpd;
	Caller caller = JSONAPI.instance.jsonServer.getCaller();
	
	public JSONResponse(JSONObject req, NanoHTTPD httpd, boolean stream) {
		this.httpd = httpd;
		this.stream = stream;
		if(req.containsKey("tag")) {
			tag = req.get("tag").toString();
		}
		
		methodName = req.get("name").toString();
		username = req.get("username").toString();
		key = req.get("key").toString();
		
		if(req.containsKey("show_previous")) {
			showOlder = Boolean.valueOf(req.get("show_previous").toString());
		}
		
		Object args = req.get("arguments");
		if(args != null && args instanceof JSONArray) {
			arguments = (JSONArray) args;
		}
	}
	
	public JSONObject getJSONObject() {
		JSONAPIAuthResponse auth = testLogin(stream);
		if(!auth.isAuthenticated()) {
			return APIError(auth.getMessage(), 8);
		}
		else if (!auth.isAllowed()) {
			return APIError(auth.getMessage(), 9);
		}
		
		return serveAPICall(arguments);
	}
	
	JSONAPIAuthResponse testLogin(boolean stream) {
		JSONAPIAuthEvent auth = new JSONAPIAuthEvent(new JSONAPIAuthResponse(true, false), methodName, username, stream);
		try {
			HashMap<String, String> logins = JSONAPI.instance.getAuthTable();
			
			if(logins.containsKey(username)) {
				String thishash = JSONAPI.SHA256(username + methodName + logins.get(username) + JSONAPI.instance.salt);

				if (thishash.equals(key)) {
					auth.getAuthResponse().setAuthenticated(true);
				}
			}
			
			Bukkit.getPluginManager().callEvent(auth);
		} catch (Exception e) {
		}
		return auth.getAuthResponse();
	}
	
	/*
	 * Error codes:
	 * 
	 * 1	Page not found
	 * 2	Invalid JSON submitted in request
	 * 3	Server offline
	 * 4	API Error
	 * 5	InvocationTargetException
	 * 6	Other caught exception
	 * 7	Method does not exist
	 * 8	The API key is wrong
	 * 9	Not allowed, but correct API key
	 * 
	 */
	@SuppressWarnings("unchecked")
	public JSONObject serveAPICall(Object args) {
		try {
			if (caller.methodExists(methodName)) {
				if (!(args instanceof JSONArray)) {
					args = new JSONArray();
				}
				Object result = caller.call(methodName, (Object[]) ((ArrayList<Object>) args).toArray(new Object[((ArrayList<Object>) args).size()]));
				return APISuccess(result);
			} else {
				JSONAPI.instance.outLog.warning("The method '" + methodName + "' does not exist!");
				return APIError("The method '" + methodName + "' does not exist!", 7);
			}
		} catch (APIException e) {
			return APIError(e.getMessage(), 4);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof APIException) {
				return APIError(e.getCause().getMessage(), 4);
			}
			return APIException(e, 5);
		} catch (NullPointerException e) {
			return APIError("The server is offline right now. Try again in 6 seconds.", 3);
		} catch (Exception e) {
			return APIException(e, 6);
		}
	}
	
	public JSONObject APIException(Exception e, int errorCode) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		StringWriter pw = new StringWriter();
		e.printStackTrace(new PrintWriter(pw));
		e.printStackTrace();
		r.put("method_name", methodName);
		
		JSONObject err_obj = new JSONObject();
		err_obj.put("message", "Caught exception: " + pw.toString().replaceAll("\\n", "\n").replaceAll("\\r", "\r"));
		err_obj.put("code", errorCode);

		r.put("error", err_obj);
		return r;
	}

	public JSONObject APIError(String error, int errorCode) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		r.put("method_name", methodName);
		
		JSONObject err_obj = new JSONObject();
		err_obj.put("message", error);
		err_obj.put("code", errorCode);
		
		r.put("error", err_obj);
		return r;
	}

	public JSONObject APISuccess(Object result) {
		JSONObject r = new JSONObject();
		r.put("result", "success");
		if(methodName != null) r.put("method_name", methodName);
		r.put("success", result);
		return r;
	}

	public String getTag() {
		return tag;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getUsername() {
		return username;
	}

	public String getKey() {
		return key;
	}

	public JSONArray getArguments() {
		return arguments;
	}

	public boolean isStream() {
		return stream;
	}

	public boolean isShowOlder() {
		return showOlder;
	}
}
