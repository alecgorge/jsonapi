package com.alecgorge.minecraft.jsonapi.util;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simpleForBukkit.JSONObject;

public class TickRateCounter {
	volatile int expectedTicks;
	volatile int elapsedTicks;
	volatile double clockRate;
	double expectedClockRate = 20.0;
	volatile long expectedTime;
	volatile long elapsedTime;
	volatile double error;
	
	int expectedSecs = 30;
	
	long start;
	long startTicks;
	World world;
	
	int task;
	JavaPlugin plugin;
	
	public TickRateCounter(JavaPlugin plugin) {  
		this.plugin = plugin;
		
        world = (World)plugin.getServer().getWorlds().get(0);
        setBaseline();
        
		task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new TickRateRepeater(), expectedTicks, expectedTicks);
	}
	
	public void cancel() {
		plugin.getServer().getScheduler().cancelTask(task);
	}
	
	class TickRateRepeater implements Runnable {
		@Override
		public void run() {
            long now = System.currentTimeMillis();
            long nowTicks = world.getFullTime();

            elapsedTime = now - start;
            double elapsedSecs = elapsedTime / 1000.0;
            elapsedTicks = (int) (nowTicks - startTicks);

            error = (expectedTime - elapsedTime) / elapsedTime * 100;
            clockRate = elapsedTicks / elapsedSecs;
            
            setBaseline();
		}
	}
	
	void setBaseline() {
        expectedTime = expectedSecs * 1000;
        expectedTicks = 20 * expectedSecs;
        start = System.currentTimeMillis();
        startTicks = world.getFullTime();
	}
	
	public JSONObject getJSONObject() {
        JSONObject o = new JSONObject();
        o.put("expectedTicks", expectedTicks);
        o.put("elapsedTicks", elapsedTicks);
        o.put("clockRate", clockRate);
        o.put("expectedClockRate", expectedClockRate);
        o.put("expectedTime", expectedTime);
        o.put("elapsedTime", elapsedTime);
        o.put("error", error);
        
        return o;
	}

	public int getExpectedTicks() {
		return expectedTicks;
	}

	public int getElapsedTicks() {
		return elapsedTicks;
	}

	public double getClockRate() {
		return clockRate;
	}

	public double getExpectedClockRate() {
		return expectedClockRate;
	}

	public long getExpectedTime() {
		return expectedTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public double getError() {
		return error;
	}
}
