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
import org.bukkit.scheduler.BukkitTask;

import com.alecgorge.java.http.MutableHttpRequest;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.streams.StreamManager;

public class StreamPusher implements JSONAPIStreamListener {
	private Map<String, List<URL>> urls = Collections.synchronizedMap(new HashMap<String, List<URL>>());
	private StreamManager manager;

	private List<BukkitTask> scheduledTasks = Collections.synchronizedList(new ArrayList<BukkitTask>());
	private Map<String, List<JSONAPIStreamMessage>> queuedMessages = Collections.synchronizedMap(new HashMap<String, List<JSONAPIStreamMessage>>());

	private File config_location;
	private YamlConfiguration config;

	private Logger log = JSONAPI.instance.outLog;
	
	int maxQueueAge = 30;
	int maxQueueLength = 500;
	String pushTag = null;

	public StreamPusher(StreamManager m, File file, int max_queue_age, int max_queue_length) {
		manager = m;
		config_location = file;
		maxQueueAge = max_queue_age;
		maxQueueLength = max_queue_length;

		try {
			if (!config_location.exists()) {
				config_location.createNewFile();
			}
			config = YamlConfiguration.loadConfiguration(config_location);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> locs = (List<Map<String, Object>>) config.getList("locations");

			if (locs == null) {
				return;
			}

			for (Map<String, Object> s : locs) {
				if ((Boolean) s.get("enabled")) {
					try {
						subscribe(s.get("stream_name").toString(), s.get("url").toString(), false);
					} catch (MalformedURLException e) {
						log.severe("[JSONAPI] Malformed URL: " + s.get("url"));
					} catch (Exception e) {
						log.severe("[JSONAPI] Non-exsistant stream: " + s.get("stream_name"));
						e.printStackTrace();
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

				List<JSONAPIStreamMessage> messages = null;
				synchronized (queuedMessages) {
					List<JSONAPIStreamMessage> a = queuedMessages.get(streamName);
					messages = new ArrayList<JSONAPIStreamMessage>(a);
					a.clear();
				}

				for (URL u : urlsToPost) {
					try {
						MutableHttpRequest r = new MutableHttpRequest(u);

						r.addPostValue("source", streamName);
						r.addPostValue("count", messages.size());
						r.addPostValue("server-name", JSONAPI.instance.getServer().getServerName());
						
						if(getPushTag() != null) {
							r.addPostValue("tag", getPushTag());
						}
						
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

			for (BukkitTask i : scheduledTasks) {
				i.cancel();
			}

			if (urls.containsKey(streamName)) {
				if (!queuedMessages.containsKey(streamName)) {
					queuedMessages.put(streamName, new ArrayList<JSONAPIStreamMessage>());
				}
				queuedMessages.get(streamName).add(message);

				// schedule it for later if the queue isn't too long.
				if(queuedMessages.size() <= maxQueueLength) {
					scheduledTasks.add(scheduler.runTaskLaterAsynchronously(JSONAPI.instance, delayedPush(streamName), 20 * maxQueueAge));
				}
				else {
					(new Thread(delayedPush(streamName))).start();
				}
			}
		}
	}

	public int getMaxQueueAge() {
		return maxQueueAge;
	}

	public void setMaxQueueAge(int maxQueueAge) {
		this.maxQueueAge = maxQueueAge;
	}

	public int getMaxQueueLength() {
		return maxQueueLength;
	}

	public void setMaxQueueLength(int maxQueueLength) {
		this.maxQueueLength = maxQueueLength;
	}
	
	public String getPushTag() {
		return pushTag;
	}

	public void setPushTag(String pushTag) {
		this.pushTag = pushTag;
	}
}
