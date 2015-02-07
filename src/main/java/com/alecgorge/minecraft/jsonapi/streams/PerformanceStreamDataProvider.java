package com.alecgorge.minecraft.jsonapi.streams;

import org.bukkit.plugin.Plugin;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.dynamic.APIWrapperMethods;


public class PerformanceStreamDataProvider implements Runnable {

	public static void enqueue(Plugin plugin) {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new PerformanceStreamDataProvider(), 100, 100);
	}
	
	@Override
	public void run() {
		JSONObject t = JSONAPI.instance.getTickRateCounter().getJSONObject();
		double diskMax = APIWrapperMethods.getInstance().getDiskSize();
		double diskUsage = APIWrapperMethods.getInstance().getDiskUsage();
		double memoryMax = APIWrapperMethods.getInstance().getJavaMaxMemory();
		double memoryUsage = APIWrapperMethods.getInstance().getJavaMemoryUsage();
		int players = JSONAPI.instance.getServer().getOnlinePlayers().size();
		JSONAPI.instance.getJSONServer().performance.addMessage(new PerformanceMessage(t, diskMax, diskUsage, memoryMax, memoryUsage, players));
	}

}
