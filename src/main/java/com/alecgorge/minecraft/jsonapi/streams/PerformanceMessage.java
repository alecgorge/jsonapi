package com.alecgorge.minecraft.jsonapi.streams;

import java.util.Map;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class PerformanceMessage extends JSONAPIStreamMessage {
	Map<String, Object> tickInformation;
	double maxDisk;
	double diskUsage;
	double maxMemory;
	double memoryUsage;
	int players;
	
	public PerformanceMessage(Map<String, Object> t, double diskMax, double diskUsage, double memoryMax, double memoryUsage, int players) {
		tickInformation = t;
		maxDisk = diskMax;
		this.diskUsage = diskUsage;
		this.maxMemory = memoryMax;
		this.memoryUsage = memoryUsage;
		this.players = players;
		
		setTime();
	}
	
	public String streamName () {
		return "performance";
	}

	public Map<String, Object> toJSONObject() {
		Map<String, Object> o = tickInformation;
		o.put("time", getTime());
		o.put("diskMax", maxDisk);
		o.put("diskUsage", diskUsage);
		o.put("memoryMax", maxMemory);
		o.put("memoryUsage", memoryUsage);
		o.put("players", players);
		return o;
	}
}
