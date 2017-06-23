package com.alecgorge.minecraft.jsonapi.api.v2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.APIException;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIAuthResponse;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;

public class JSONResponse {
	String tag = "";
	String methodName;
	String username;
	String key;
	JSONArray arguments;
	JSONAPIAuthResponse auth;
	boolean stream = false;
	boolean showOlder = false;
	
	Caller caller = JSONAPI.instance.jsonServer.getCaller();
	
	JSONObject error = null;
	
	public JSONResponse(JSONObject req, boolean stream) {
		this.stream = stream;
		if(req.containsKey("tag")) {
			tag = req.get("tag").toString();
		}
		
		methodName = req.get("name").toString();
		
		if(!req.containsKey("username")) {
			error = APIError("Missing username from payload", 10);
			return;
		}
		
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
		if(error != null) {
			return error;
		}
		
		JSONAPIAuthResponse auth = testLogin(stream);
		if(!auth.isAuthenticated()) {
			return APIError(auth.getMessage(), 8);
		}
		else if (!auth.isAllowed()) {
			return APIError(auth.getMessage(), 9);
		}
		
		return serveAPICall(arguments);
	}
	
	public JSONAPIAuthResponse testLogin(boolean stream) {
		if(this.auth != null) {
			return this.auth;
		}
		JSONAPIAuthEvent auth = new JSONAPIAuthEvent(new JSONAPIAuthResponse(false, false), methodName, username, stream);
		try {
			UsersConfig logins = JSONAPI.instance.getAuthTable();
			
			if(logins.userExists(username)) {
				String saltless = JSONAPI.SHA256(username + methodName + logins.getUser(username).getPassword());
				String salted = JSONAPI.SHA256(username + methodName + logins.getUser(username).getPassword() + JSONAPI.instance.salt);

				if (saltless.equals(key) || salted.equals(key)) {
					auth.getAuthResponse().setAuthenticated(true);
				}
			}
			
			JSONAPI.dbug("Calling auth event " + auth);
			Bukkit.getPluginManager().callEvent(auth);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.auth = auth.getAuthResponse();
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
	 * 10	Missing username from payload
	 * 
	 */
	@SuppressWarnings("unchecked")
	public JSONObject serveAPICall(Object args) {
		try {
			if(methodName.equals("chat.with_name")) {
				JSONAPIUser u = UsersConfig.config().getUser(username);
				if(!u.hasPermission("change_chat_name") && !u.canUseMethod("ALLOW_ALL")) {
					((ArrayList<Object>) args).set(1, username);
				}
			}
			
			if (caller.methodExists(methodName)) {
				if (!(args instanceof JSONArray)) {
					args = new JSONArray();
				}
				Object result = caller.call(methodName, (Object[]) ((ArrayList<Object>) args).toArray(new Object[((ArrayList<Object>) args).size()]));
				return APISuccess(result);
			} else {
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
		} catch (Throwable e) {
			return APIException(e, 6);
		}
	}
	
	public JSONObject APIException(Throwable e, int errorCode) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		r.put("is_success", false);
		StringWriter pw = new StringWriter();
		e.printStackTrace(new PrintWriter(pw));
		e.printStackTrace();
		r.put("source", methodName);
		
		JSONObject err_obj = new JSONObject();
		err_obj.put("message", "Caught exception: " + pw.toString().replaceAll("\\n", "\n").replaceAll("\\r", "\r"));
		err_obj.put("code", errorCode);

		r.put("error", err_obj);
		
		if(!tag.equals("")) {
			r.put("tag", tag);
		}
		
		return r;
	}
	
	public JSONObject APIError(String error, int errorCode) {
		return APIError(error, errorCode, methodName, tag);
	}

	public static JSONObject APIError(String error, int errorCode, String methodName, String tag) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		r.put("source", methodName);
		r.put("is_success", false);
		
		JSONObject err_obj = new JSONObject();
		err_obj.put("message", error);
		err_obj.put("code", errorCode);
		
		r.put("error", err_obj);
		
		if(!tag.equals("")) {
			r.put("tag", tag);
		}

		return r;
	}

	public JSONObject APISuccess(Object result) {
		JSONObject r = new JSONObject();
		r.put("result", "success");
		r.put("is_success", true);
		if(methodName != null) r.put("source", methodName);
		r.put("success", result);
		
		if(!tag.equals("")) {
			r.put("tag", tag);
		}

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
	
	public JSONAPIAuthResponse auth() {
		return this.auth;
	}
}
