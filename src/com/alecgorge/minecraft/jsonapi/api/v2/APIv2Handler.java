package com.alecgorge.minecraft.jsonapi.api.v2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD;
import com.alecgorge.minecraft.jsonapi.NanoHTTPD.Response;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class APIv2Handler {
	String uri;
	String method;
	Properties header;
	Properties params;
	
	NanoHTTPD httpd;
	
	List<JSONResponse> requests;
	
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
		for(JSONResponse resp : requests) {
			a.add(resp.getJSONObject());
		}
		
		return resp(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_JSON, a.toJSONString());
	}
	
	public NanoHTTPD.Response resp(String resp, String type, String body) {
		if(params != null && type.equals(NanoHTTPD.MIME_JSON) && params.containsKey("callback")) {
			body = params.getProperty("callback") + "(" + body + ");";
		}
		return httpd.new Response(resp, type, body);
	}
	
	public NanoHTTPD.Response subscribe() {
		List<String> 	sourceLists = new ArrayList<String>();
		List<Boolean> 	showOlder 	= new ArrayList<Boolean>();
		List<String> 	tag 		= new ArrayList<String>();
		
		for(JSONResponse resp : requests) {
			sourceLists.add(resp.getMethodName());
			showOlder.add(resp.isShowOlder());
			tag.add(resp.getTag());
		}
		
		StreamingResponse streams = new StreamingResponse(JSONAPI.instance, sourceLists, params.getProperty("callback"), showOlder, tag);
		
		return httpd.new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, streams);
	}
	
	public void readPayload(boolean stream) throws ParseException {
		if(params.containsKey("json")) {
			JSONArray requests = new JSONArray();
			Object o = parser.parse(params.get(params).toString());
			
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
		}
	}	
}
