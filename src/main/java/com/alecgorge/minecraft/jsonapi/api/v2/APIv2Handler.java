package com.alecgorge.minecraft.jsonapi.api.v2;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIAuthResponse;
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
			else {
				return resp(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
			}
		}
		catch (ParseException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			return resp(NanoHTTPD.HTTP_BADREQUEST, NanoHTTPD.MIME_PLAINTEXT, errors.toString());
		}
	}
	
	public NanoHTTPD.Response call() {
		JSONArray a = new JSONArray();
		
		boolean allAreAuth = true;
		for(JSONResponse resp : requests) {
			if(!resp.testLogin(false).isAuthenticated()) {
				allAreAuth = false;
			}
			a.add(resp.getJSONObject());
		}
		
		return resp(allAreAuth ? NanoHTTPD.HTTP_OK : NanoHTTPD.HTTP_FORBIDDEN, NanoHTTPD.MIME_JSON, a.toJSONString());
	}
	
	public NanoHTTPD.Response resp(String resp, String type, String body) {
		if(params != null && type.equals(NanoHTTPD.MIME_JSON) && params.containsKey("callback")) {
			body = params.getProperty("callback") + "(" + body + ");";
		}
		Response r = httpd.new Response(resp, type, body);
		r.addHeader("Access-Control-Allow-Origin", "*");

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
		if(params.containsKey("json")) {
			Object o = parser.parse(params.get("json").toString());
			
			if(o instanceof JSONObject) {
				requests.add(new JSONResponse((JSONObject)o, httpd, stream));
			}
			else if(o instanceof JSONArray) {
				for(Object obj : (JSONArray) o) {
					if(obj instanceof JSONObject) {
						requests.add(new JSONResponse((JSONObject)obj, httpd, stream));
					}
				}
			}
			
			// logging
			StringBuilder b = new StringBuilder("[JSONAPI] ");
			b.append(stream ? "[Stream Request] " : "[API Request] ");
			
			for(JSONResponse r : requests) {
				b.append(r.getMethodName()).append("(").append(r.getArguments()).append(") ");
			}
			
			jsonapiLog.info(b.toString());
		}
	}	
}
