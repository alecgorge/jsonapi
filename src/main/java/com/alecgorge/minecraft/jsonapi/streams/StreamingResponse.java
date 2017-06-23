package com.alecgorge.minecraft.jsonapi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.JSONServer;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamListener;
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class StreamingResponse extends InputStream implements JSONAPIStreamListener {
	private List<JSONAPIStream> stacks = new ArrayList<JSONAPIStream>();
	private LinkedBlockingQueue<JSONAPIStreamMessage> queue = new LinkedBlockingQueue<JSONAPIStreamMessage>();
	private JSONAPI plugin;
	private String callback;
	private List<String> tag;
	private List<String> streams;
	
	public StreamingResponse(JSONAPI _plugin, List<String> sourceLists, String callback, List<Boolean> showOlder, List<String> tag, List<JSONObject> seed) {
		plugin = _plugin;
		this.tag = tag;
		
		this.streams = sourceLists;
		for(String s : sourceLists) {
			if(plugin.getStreamManager().streamExists(s)) {
				stacks.add(plugin.getStreamManager().getStream(s));
			}
			else {
				plugin.outLog.warning("The requested stream: '"+s+"' does not exist.");
			}
		}
		
		int i = 0;
		
		JSONAPI.dbug("Stacks: " + stacks);
		for(JSONAPIStream s : stacks) {
			JSONAPI.dbug("adding listener for " + s.getName() + ": " + s.getClass());
			s.registerListener(this, showOlder.get(i));
			i++;
		}
		
		if(seed != null) {
			for(JSONObject o : seed) {
				onMessage(new JSONObjectMessage(o), null);
			}
		}
	}
	
	public StreamingResponse(JSONAPI _plugin, List<String> sourceLists, String callback, boolean showOlder, String tag) {
		plugin = _plugin;
		
		List<String> tags = new ArrayList<String>(sourceLists.size());
		for(int i = 0; i < sourceLists.size(); i++) {
			tags.add(tag);
		}
		this.tag = tags;
		
		this.streams = sourceLists;
		for(String s : sourceLists) {
			if(plugin.getStreamManager().streamExists(s)) {
				stacks.add(plugin.getStreamManager().getStream(s));
			}
			else {
				plugin.outLog.warning("The requested stream: '"+s+"' does not exist.");
			}
		}
		
		for(JSONAPIStream s : stacks) {
			JSONAPI.dbug("adding listener for " + s.getName());
			s.registerListener(this, showOlder);
		}
	}
		
	public void onMessage(JSONAPIStreamMessage message, JSONAPIStream sender) {
		JSONAPI.dbug("recieveing message for " + (sender == null ? null : String.valueOf(sender.getName())));
		try {
			queue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String nextLine () {
		try {
			while(true) {
				JSONAPIStreamMessage m;
				m = queue.take();
				return  JSONServer.callback(callback, makeResponseObj(m)).concat("\r\n");
			}
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	@Override
	public void close() throws IOException {
		for(JSONAPIStream s : stacks) {
			JSONAPI.dbug("removing listener for " + s.getName());
			s.deregisterListener(this);
		}
	}
	
	private String makeResponseObj (JSONAPIStreamMessage ja) {
		if(ja instanceof JSONObjectMessage) {
			return ja.toJSONObject().toJSONString();
		}
		
		JSONObject o = new JSONObject();
		o.put("result", "success");
		o.put("source", ja.streamName());
		o.put("success", ja);
		
		try {
			o.put("tag", tag.get(streams.indexOf(ja.streamName())));
			return o.toJSONString();
		}
		catch (Exception e) {
			return o.toJSONString(); // incase of a concurrence issue
									 // very hacky but it works
		}		
	}
	
	public int read() throws IOException {
		return -1;
	}
}
