package com.alecgorge.minecraft.jsonapi.packets.netty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.api.v2.JSONResponse;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIAuthResponse;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class APIv2Handler {
	static Logger jsonapiLog = JSONAPI.instance.outLog;
	static String CONTENT_TYPE_JSON = "application/json";
	
	FullHttpRequest request;
	QueryStringDecoder uri;
	
	List<JSONResponse> requests = new ArrayList<JSONResponse>();
	
	JSONParser parser = new JSONParser();
	
	public APIv2Handler (FullHttpRequest req) {
		request = req;
		uri = new QueryStringDecoder(request.getUri());
	}
	
	public static boolean canServe(QueryStringDecoder u) {
		JSONAPI.dbug("can serve? " + u.path());
		
		return u.path().startsWith("/api/2/") && !u.path().equals("/api/2/websocket");
	}
	
	public FullHttpResponse serve() {
		try {
			if(uri.path().equals("/api/2/call")) {
				readPayload(false);
				return call();
			}
			// just use websocket for subscriptions, ok?
//			else if(uri.path().equals("/api/2/subscribe")) {
//				readPayload(true);
//				return subscribe();
//			}
			else if(uri.path().equals("/api/2/version")) {
				return version();
			}
			else {
				return resp(HttpResponseStatus.NOT_FOUND, "text/plain", "Not found.\n");
			}
		}
		catch (ParseException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			
			return resp(HttpResponseStatus.BAD_REQUEST, CONTENT_TYPE_JSON, "["+JSONResponse.APIError(errors.toString(), 4, "JSON_PARSE_ERROR", "").toJSONString()+"]\n");
		}
	}
	
	public boolean isStream() {
		return uri.path().equals("/api/2/subscribe");
	}

	public FullHttpResponse call() {
		JSONArray a = new JSONArray();
		
		for(JSONResponse resp : requests) {
			a.add(resp.getJSONObject());
		}
		
		String json = a.toJSONString();
		
		JSONAPI.dbug("returning: " + json);
		return resp(HttpResponseStatus.OK, CONTENT_TYPE_JSON, json + "\n");
	}
	
	public FullHttpResponse version() {
		JSONObject versionObj = new JSONObject();
		versionObj.put("version", JSONAPI.instance.getDescription().getVersion());
		versionObj.put("server_version", JSONAPI.instance.getServer().getVersion());
		
		return resp(HttpResponseStatus.OK, CONTENT_TYPE_JSON, versionObj.toJSONString());
	}
	
	public FullHttpResponse resp(HttpResponseStatus resp, String type, String body) {
		if(uri.parameters().containsKey("callback") && type.equals(CONTENT_TYPE_JSON)) {
			body = uri.parameters().get("callback") + "(" + body + ");";
		}
		
		ByteBuf buf = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
		FullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, resp, buf);
		r.headers().set("Access-Control-Allow-Origin", "*");
		r.headers().set("Content-Length", buf.readableBytes());
		r.headers().set("Content-Type", type);

		return r;
	}
	
	public StreamingResponse subscribe() {
		try {
			readPayload(true);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

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
		
		StreamingResponse streams = new StreamingResponse(JSONAPI.instance, sourceLists, uri.parameters().containsKey("callback") ? uri.parameters().get("callback").get(0) : null, showOlder, tag, defaults);
		
		return streams;
	}
	
	public void readPayload(boolean stream) throws ParseException {
        String json = null;
        if (uri.parameters().containsKey("json")) {
            json = uri.parameters().get("json").get(0);
        } else {
            ByteBuf byteBuf = request.content();
            if (byteBuf.isReadable()) {
                json = byteBuf.toString(Charset.forName("UTF-8"));
            }
        }

        if (json != null) {
            Object o = parser.parse(json);
			
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
