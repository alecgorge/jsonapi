package com.alecgorge.minecraft.jsonapi.streams;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStream;

public class StreamManager {
	private Map<String,JSONAPIStream> streams = Collections.synchronizedMap(new HashMap<String,JSONAPIStream>());
	
	public void registerStream(String streamName, JSONAPIStream stream) {
		streams.put(streamName, stream);
	}
	
	public void deregisterStream(String streamName) {
		streams.remove(streams.get(streamName));
	}
	
	public boolean streamExists(String streamName) {
		return streams.containsKey(streamName);
	}
	
	public JSONAPIStream getStream(String streamName) {
		return streams.get(streamName);
	}
	
	public Map<String, JSONAPIStream> getStreams() {
		return streams;
	}
}
