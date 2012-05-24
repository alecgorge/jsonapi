package com.alecgorge.minecraft.jsonapi.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.alecgorge.java.http.HttpRequest;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.streams.StreamManager;

public class StreamPusher implements JSONAPIStreamListener {
	private Map<String, List<URL>> urls = Collections.synchronizedMap(new HashMap<String, List<URL>>());
	private StreamManager manager;

	private List<Integer> scheduledTasks = Collections.synchronizedList(new ArrayList<Integer>());
	private Map<String, List<JSONAPIStreamMessage>> queuedMessages = Collections.synchronizedMap(new HashMap<String, List<JSONAPIStreamMessage>>());

	private File config_location;
	private YamlConfiguration config;

	private Logger log = Logger.getLogger("JSONAPI");

	public StreamPusher(StreamManager m, File file) {
		manager = m;
		config_location = file;

		try {
			if (!config_location.exists()) {
				config_location.createNewFile();
			}
			config = YamlConfiguration.loadConfiguration(config_location);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> locs = (List<Map<String, Object>>) config.getList("locations");

			for (Map<String, Object> s : locs) {
				if ((Boolean) s.get("enabled")) {
					try {
						subscribe(s.get("stream_name").toString(), s.get("url").toString(), false);
					} catch (MalformedURLException e) {
						log.severe("Malformed URL: " + s.get("url"));
					} catch (Exception e) {
						log.severe("Non-exsistant stream: " + s.get("stream_name"));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void subscribe(String streamName, String Surl, boolean appendToYml) throws MalformedURLException, Exception {
		URL url = new URL(Surl);

		subscribe(streamName, url);

		if (appendToYml) {
			@SuppressWarnings("unchecked")
			List<ConfigurationSection> locs = (List<ConfigurationSection>) config.getList("locations");

			ConfigurationSection s = new MemoryConfiguration();
			s.set("stream_name", streamName);
			s.set("enabled", true);
			s.set("url", Surl);

			locs.add(s);

			config.save(config_location);
		}
	}

	public void subscribe(String streamName, URL urlToPushTo) throws Exception {
		JSONAPIStream s = manager.getStream(streamName);
		if (s == null) {
			throw new Exception(streamName + " does not exist.");
		}

		boolean didContainKey = urls.containsKey(streamName);
		if (!didContainKey) {
			urls.put(streamName, new ArrayList<URL>());
		}

		urls.get(streamName).add(urlToPushTo);

		// this is down here because we don't want a race condition
		if (!didContainKey) {
			s.registerListener(this, false);
		}
	}

	private Runnable delayedPush(final String streamName) {
		return new Runnable() {
			@Override
			public void run() {
				List<URL> urlsToPost = urls.get(streamName);
				List<JSONAPIStreamMessage> messages = queuedMessages.get(streamName);

				for (URL u : urlsToPost) {
					try {
						HttpRequest r = new HttpRequest(u);

						r.addPostValue("source", streamName);
						r.addPostValue("count", messages.size());
						for (JSONAPIStreamMessage m : messages) {
							r.addPostValue("messages[]", m.toJSONString());
						}

						r.post();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
	}

	@Override
	public void onMessage(JSONAPIStreamMessage message, JSONAPIStream sender) {
		String streamName = sender.getName();

		synchronized (scheduledTasks) {
			BukkitScheduler scheduler = Bukkit.getScheduler();

			for (Integer i : scheduledTasks) {
				scheduler.cancelTask(i);
			}

			if (urls.containsKey(streamName)) {
				if (!queuedMessages.containsKey(streamName)) {
					queuedMessages.put(streamName, new ArrayList<JSONAPIStreamMessage>());
				}
				queuedMessages.get(streamName).add(message);

				// 200 ticks is 10 seconds
				scheduledTasks.add(scheduler.scheduleAsyncDelayedTask(JSONAPI.instance, delayedPush(streamName), 200));
			}
		}
	}
}
