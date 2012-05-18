package com.alecgorge.minecraft.jsonapi.api;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import com.alecgorge.java.http.HttpRequest;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.streams.StreamManager;

public class StreamPusher implements JSONAPIStreamListener {
	private Map<String, List<URL>> urls = Collections.synchronizedMap(new HashMap<String, List<URL>>());
	private StreamManager manager;

	private List<Integer> scheduledTasks = Collections.synchronizedList(new ArrayList<Integer>());
	private Map<String, List<JSONAPIStreamMessage>> queuedMessages = Collections.synchronizedMap(new HashMap<String, List<JSONAPIStreamMessage>>());

	public StreamPusher(StreamManager m) {
		manager = m;
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
