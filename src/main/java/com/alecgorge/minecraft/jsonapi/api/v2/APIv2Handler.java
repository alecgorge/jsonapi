package com.alecgorge.minecraft.jsonapi.api.v2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD.Response;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIAuthResponse;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class APIv2Handler {
	static Logger jsonapiLog = JSONAPI.instance.outLog;
	
	String uri;
	String method;
	Properties header;
	Properties params;
	
	NanoHTTPD httpd;
	
	List<JSONResponse> requests = new ArrayList<JSONResponse>();
	
	JSONParser parser = new JSONParser();
	
	public APIv2Handler (String uri, String method, Properties header, Properties parms, NanoHTTPD httpd) {
		this.uri = uri;
		this.method = method;
		this.header = header;
		this.params = parms;
		this.httpd = httpd;
	}
	
	public Response serve() {
		try {
			if(this.uri.equals("/api/2/call")) {
				readPayload(false);
				return call();
			}
			else if(this.uri.equals("/api/2/subscribe")) {
				readPayload(true);
				return subscribe();
			}
			else if(this.uri.equals("/api/2/version")) {
				return version();
			}
			else if(this.uri.equals("/api/2/websocket")) {
				JSONAPI.dbug("websocket requested");
				return reverseProxyWebSocket();
			}
			else {
				return resp(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
			}
		}
		catch (ParseException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			return resp(NanoHTTPD.HTTP_BADREQUEST, NanoHTTPD.MIME_JSON, "["+JSONResponse.APIError(errors.toString(), 4, "JSON_PARSE_ERROR", "").toJSONString()+"]");
		}
	}
	
	Response reverseProxyWebSocket() {
		return httpd.new WebSocketResponse(header);
	}
	
	public NanoHTTPD.Response call() {
		JSONArray a = new JSONArray();
		
		for(JSONResponse resp : requests) {
			a.add(resp.getJSONObject());
		}
		
		String json = a.toJSONString();
		
		JSONAPI.dbug("returning: " + json);
		return resp(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_JSON, json);
	}
	
	public NanoHTTPD.Response version() {
		JSONObject versionObj = new JSONObject();
		versionObj.put("version", JSONAPI.instance.getDescription().getVersion());
		versionObj.put("server_version", JSONAPI.instance.getServer().getVersion());
		
		return resp(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_JSON, versionObj.toJSONString());
	}
	
	public NanoHTTPD.Response resp(String resp, String type, String body) {
		if(params != null && type.equals(NanoHTTPD.MIME_JSON) && params.containsKey("callback")) {
			body = params.getProperty("callback") + "(" + body + ");";
		}

		byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
		Response r = httpd.new Response(resp, type, bytes);
		r.addHeader("Access-Control-Allow-Origin", "*");
		r.addHeader("Content-Length", String.valueOf(bytes.length));

		return r;
	}
	
	public NanoHTTPD.Response subscribe() {
		List<String> 	sourceLists = new ArrayList<String>();
		List<Boolean> 	showOlder 	= new ArrayList<Boolean>();
		List<String> 	tag 		= new ArrayList<String>();
		List<JSONObject>defaults	= new ArrayList<JSONObject>();
		
		
		for(JSONResponse resp : requests) {
			JSONAPIAuthResponse auth = resp.testLogin(true);
			if(auth.isAllowed() && auth.isAuthenticated()) {
				sourceLists.add(resp.getMethodName());
				showOlder.add(resp.isShowOlder());
				tag.add(resp.getTag());				
			}
			else {
				defaults.add(resp.getJSONObject());
			}
		}
		
		StreamingResponse streams = new StreamingResponse(JSONAPI.instance, sourceLists, params.getProperty("callback"), showOlder, tag, defaults);
		
		Response r = httpd.new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, streams);
		r.addHeader("Access-Control-Allow-Origin", "*");

		return r;
	}
	
	public void readPayload(boolean stream) throws ParseException {
		JSONAPI.dbug("params.json: "+params.get("json"));
		if(params.containsKey("json")) {
			Object o = parser.parse(params.get("json").toString());
			
			JSONAPI.dbug("json obj: "+ o);
			
			if(o instanceof JSONObject) {
				requests.add(new JSONResponse((JSONObject)o, stream));
			}
			else if(o instanceof JSONArray) {
				for(Object obj : (JSONArray) o) {
					if(obj instanceof JSONObject) {
						requests.add(new JSONResponse((JSONObject)obj, stream));
					}
				}
			}
			
			String username = requests.get(0).getUsername();
			JSONAPIUser user = UsersConfig.config().getUser(username);
			if(user == null || user.getLogging()) {
				// logging
				StringBuilder b = new StringBuilder("[JSONAPI] ");
				b.append(stream ? "[Stream Request] " : "[API Request] ");
				b.append(requests.get(0).getUsername()).append(" requested: ");
				
				for(JSONResponse r : requests) {
					b.append(r.getMethodName()).append("(").append(r.getArguments() == null ? "" : r.getArguments()).append(")");
					
					JSONAPIAuthResponse a = r.testLogin(false);
					b.append("{").append(a.isAuthenticated() ? "AUTHED" : "NOT AUTHED").append(", ");
					b.append(a.isAllowed() ? "ALLOWED" : "NOT ALLOWED");				
					Caller c = JSONAPI.instance.jsonServer.getCaller();
					
					if(!c.methodExists(r.getMethodName()) && !JSONAPI.instance.getStreamManager().streamExists(r.getMethodName())) {
						b.append(", NO-EXIST");
					}
					
					b.append("} ");
				}
				
				jsonapiLog.info(b.toString());
			}
		}
	}	
}
